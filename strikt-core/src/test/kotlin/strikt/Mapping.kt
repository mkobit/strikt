package strikt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.first
import strikt.assertions.flatMap
import strikt.assertions.get
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.last
import strikt.assertions.map
import strikt.assertions.message
import strikt.assertions.single
import strikt.assertions.throws
import java.time.LocalDate

@DisplayName("mapping assertions")
internal class Mapping {
  @Test
  fun `map() on iterable subjects maps to an iterable`() {
    val subject = listOf("catflap", "rubberplant", "marzipan")
    expectThat(subject)
      .map { it.toUpperCase() }
      .containsExactly("CATFLAP", "RUBBERPLANT", "MARZIPAN")
  }

  @Test
  fun `first() maps to the first element of an iterable`() {
    val subject = listOf("catflap", "rubberplant", "marzipan")
    expectThat(subject).first().isEqualTo("catflap")
  }

  @Test
  fun `single() maps to the single element of an iterable`() {
    val subject = listOf("catflap")
    expectThat(subject).single().isEqualTo("catflap")
  }

  @Test
  fun `single() fails when the iterable has multiple entries`() {
    val subject = listOf("catflap", "rubberplant")
    assertThrows<AssertionError> {
      expectThat(subject).single().isEqualTo("catflap")
    }.let { error ->
      expectThat(error).message.isEqualTo(
        """▼ Expect that ["catflap", "rubberplant"]:
  ✗ has only one element"""
      )
    }
  }

  @Test
  fun `last() maps to the last element of an iterable`() {
    val subject = listOf("catflap", "rubberplant", "marzipan")
    expectThat(subject).last().isEqualTo("marzipan")
  }

  @Test
  fun `array access maps to an indexed element of a list`() {
    val subject = listOf("catflap", "rubberplant", "marzipan")
    expectThat(subject)[1].isEqualTo("rubberplant")
  }

  @Test
  fun `array access maps to a sub-list of a list`() {
    val subject = listOf("catflap", "rubberplant", "marzipan")
    expectThat(subject)[1..2].containsExactly("rubberplant", "marzipan")
  }

  @Test
  fun `array access maps to a value of a map`() {
    val subject = mapOf("foo" to "bar")
    expectThat(subject)["foo"].isNotNull().isEqualTo("bar")
    expectThat(subject)["bar"].isNull()
  }

  @Test
  fun `message maps to an exception message`() {
    expectThat(catching { error("o noes") })
      .throws<IllegalStateException>()
      .message
      .isEqualTo("o noes")
  }

  @Test
  fun `message fails if the exception message is null`() {
    assertThrows<AssertionError> {
      expectThat(catching { throw IllegalStateException() })
        .throws<IllegalStateException>()
        .message
    }.let { error ->
      expectThat(error).message.isEqualTo(
        """▼ Expect that java.lang.IllegalStateException:
          |  ✓ threw java.lang.IllegalStateException
          |  ▼ value of property message:
          |    ✗ is not null""".trimMargin()
      )
    }
  }

  @Test
  fun `first maps an iterable to its first element`() {
    val subject = listOf("catflap", "rubberplant", "marzipan", "radish")
    expectThat(subject)
      .first { it.startsWith("r") }
      .isEqualTo("rubberplant")
  }

  @Test
  fun `flatMap maps a result iterable to a flattened iterable`() {
    val subject = listOf(
      mapOf("words" to listOf("catflap", "rubberplant", "marzipan")),
      mapOf("words" to listOf("kattenluik", "rubberboom", "marsepein"))
    )
    expectThat(subject)
      .flatMap { it["words"]!! }
      .containsExactly(
        "catflap",
        "rubberplant",
        "marzipan",
        "kattenluik",
        "rubberboom",
        "marsepein"
      )
  }

  data class Person(val name: String, val birthDate: LocalDate)

  @Nested
  @DisplayName("custom mappings")
  inner class Custom {
    val subject = Person("David", LocalDate.of(1947, 1, 8))

    @Test
    fun `can map with a closure`() {
      expectThat(subject) {
        get { name }.isEqualTo("David")
        get { birthDate.year }.isEqualTo(1947)
      }
    }

    @Test
    fun `can map with property and method references`() {
      expectThat(subject) {
        get(Person::name).isEqualTo("David")
        get(Person::birthDate).get(LocalDate::getYear).isEqualTo(1947)
      }
    }

    @Test
    fun `closures can call methods`() {
      expectThat(subject) {
        get { name.toUpperCase() }.isEqualTo("DAVID")
        get { birthDate.plusYears(69).plusDays(2) }
          .isEqualTo(LocalDate.of(2016, 1, 10))
      }
    }

    @Test
    fun `can be described`() {
      val error = assertThrows<AssertionError> {
        expectThat(subject) {
          get { name }.describedAs("name").isEqualTo("Ziggy")
          get { birthDate.year }.describedAs("birth year")
            .isEqualTo(1971)
        }
      }
      assertEquals(
        """▼ Expect that Person(name=David, birthDate=1947-01-08):
          |  ▼ name:
          |    ✗ is equal to "Ziggy" : found "David"
          |  ▼ birth year:
          |    ✗ is equal to 1971 : found 1947""".trimMargin(),
        error.message
      )
    }

    @Test
    fun `descriptions are defaulted when using property references`() {
      val error = assertThrows<AssertionError> {
        expectThat(subject).get(Person::name).isEqualTo("Ziggy")
      }
      assertEquals(
        """▼ Expect that Person(name=David, birthDate=1947-01-08):
          |  ▼ value of property name:
          |    ✗ is equal to "Ziggy" : found "David"""".trimMargin(),
        error.message
      )
    }

    @Test
    fun `descriptions also default for blocks`() {
      val error = assertThrows<AssertionError> {
        expectThat(subject) {
          get { name }.isEqualTo("Ziggy")
          get {
            birthDate.year
          }.isEqualTo(1971)
        }
      }
      assertEquals(
        """▼ Expect that Person(name=David, birthDate=1947-01-08):
          |  ▼ name:
          |    ✗ is equal to "Ziggy" : found "David"
          |  ▼ birthDate.year:
          |    ✗ is equal to 1971 : found 1947""".trimMargin(),
        error.message
      )
    }

    @Test
    fun `descriptions are defaulted when using bean getter references`() {
      val error = assertThrows<AssertionError> {
        expectThat(subject).get(Person::birthDate)
          .get(LocalDate::getYear)
          .isEqualTo(1971)
      }
      assertEquals(
        """▼ Expect that Person(name=David, birthDate=1947-01-08):
          |  ▼ value of property birthDate:
          |    ▼ return value of getYear:
          |      ✗ is equal to 1971 : found 1947""".trimMargin(),
        error.message
      )
    }
  }
}

data class Person(val name: String, val birthDate: LocalDate = LocalDate.now())
data class Album(val name: String)
