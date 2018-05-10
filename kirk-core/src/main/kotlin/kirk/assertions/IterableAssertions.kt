package kirk.assertions

import kirk.api.Assertion

/**
 * Asserts that all elements of the subject pass the assertions in [predicate].
 */
fun <T : Iterable<E>, E> Assertion<T>.all(predicate: Assertion<E>.() -> Unit) =
  assert("all elements of %s match:") {
    compose {
      subject.forEach {
        expect(it, predicate)
      }
    } results {
      if (allPassed) pass() else fail()
    }
  }

/**
 * Asserts that _at least one_ element of the subject pass the assertions in
 * [predicate].
 */
fun <T : Iterable<E>, E> Assertion<T>.any(predicate: Assertion<E>.() -> Unit) =
  assert("at least one element of %s matches:") {
    compose {
      subject.forEach {
        expect(it, predicate)
      }
    } results {
      if (anyPassed) pass() else fail()
    }
  }

/**
 * Asserts that _no_ elements of the subject pass the assertions in [predicate].
 */
fun <T : Iterable<E>, E> Assertion<T>.none(predicate: Assertion<E>.() -> Unit) =
  assert("no elements of %s match:") {
    compose {
      subject.forEach {
        expect(it, predicate)
      }
    } results {
      if (allFailed) pass() else fail()
    }
  }

/**
 * Asserts that all [elements] are present in the subject.
 * The elements may exist in any order any number of times and the subject may
 * contain further elements that were not specified.
 * If either the subject or [elements] are empty the assertion always fails.
 */
fun <T : Iterable<E>, E> Assertion<T>.contains(vararg elements: E) =
  assert("%s contains the elements ${elements.toList()}") {
    when (elements.size) {
      0    -> fail() // TODO: really need a message here
      else -> {
        compose {
          elements.forEach { element ->
            expect(subject).assert("contains $element") {
              if (subject.contains(element)) {
                pass()
              } else {
                fail()
              }
            }
          }
        } results {
          if (allPassed) pass() else fail()
        }
      }
    }
  }

/**
 * Asserts that all [elements] _and no others_ are present in the subject in the
 * specified order.
 *
 * If the subject has no guaranteed iteration order (for example a [Set] this
 * assertion is probably not appropriate and you should use
 * [containsExactlyInAnyOrder] instead.
 */
fun <T : Iterable<E>, E> Assertion<T>.containsExactly(vararg elements: E) =
  assert("%s contains exactly the elements ${elements.toList()}") {
    compose {
      val original = subject.toList()
      val remaining = subject.toMutableList()
      elements.forEachIndexed { i, it ->
        assert("contains $it") {
          if (remaining.remove(it)) {
            pass()
            assert("…at index $i") {
              if (original[i] == it) {
                pass()
              } else {
                fail("found %s", original[i])
              }
            }
          } else {
            fail()
          }
        }
      }
      assert("contains no further elements") {
        if (remaining.isEmpty()) {
          pass()
        } else {
          fail("found %s", remaining)
        }
      }
    } results {
      if (allPassed) pass() else fail()
    }
  }

/**
 * Asserts that all [elements] _and no others_ are present in the subject.
 * Order is not evaluated, so an assertion on a [List] will pass so long as it
 * contains all the same elements with the same cardinality as [elements]
 * regardless of what order they appear in.
 */
fun <T : Iterable<E>, E> Assertion<T>.containsExactlyInAnyOrder(vararg elements: E) =
  assert("%s contains exactly the elements ${elements.toList()} in any order") {
    compose {
      val remaining = subject.toMutableList()
      elements.forEach {
        assert("contains $it") {
          if (remaining.remove(it)) {
            pass()
          } else {
            fail()
          }
        }
      }
      assert("contains no further elements") {
        if (remaining.isEmpty()) {
          pass()
        } else {
          fail("found %s", remaining)
        }
      }
    } results {
      if (allPassed) pass() else fail()
    }
  }

