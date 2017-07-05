package com.datamountaineer.streamreactor.connect.elastic.indexname

import org.scalatest.prop.{GeneratorDrivenPropertyChecks, TableDrivenPropertyChecks}
import org.scalatest.{FlatSpec, Matchers}

class TestCustomIndexName extends FlatSpec with TableDrivenPropertyChecks with GeneratorDrivenPropertyChecks with Matchers {

  val ValidIndexNames = Table(
    ("Valid index name", "Expectations"),
    ("", Vector()),
    ("abc", Vector(TextFragment("abc"))),
    ("abc{YYYY-MM-dd}", Vector(TextFragment("abc"), DateTimeFragment("YYYY-MM-dd"))),
    ("{YYYY-MM-dd}abc", Vector(DateTimeFragment("YYYY-MM-dd"), TextFragment("abc"))),
    ("{YYYY-MM-dd}abc{HH-MM-ss}", Vector(DateTimeFragment("YYYY-MM-dd"), TextFragment("abc"), DateTimeFragment("HH-MM-ss"))),
    ("{YYYY-MM-dd}{HH-MM-ss}", Vector(DateTimeFragment("YYYY-MM-dd"), DateTimeFragment("HH-MM-ss"))),
    ("abc{}", Vector(TextFragment("abc"))),
    ("{}abc", Vector(TextFragment("abc")))
  )

  val InvalidIndexNames = Table(
    ("Invalid index name"),
    ("}abc"),
    ("abc}"),
    ("abc}def")
  )

  "Custom index name" should "parse a valid String with date time formatting options" in {
    forAll (ValidIndexNames) { case (validIndexName, expectations) =>
      CustomIndexName.parseIndexName(validIndexName) shouldBe CustomIndexName(expectations)
    }
  }

  it should "throw an exception when using invalid index name" in {
    forAll (InvalidIndexNames) { case (invalidIndexName) =>
      intercept[InvalidCustomIndexNameException] {
        CustomIndexName.parseIndexName(invalidIndexName)
      }
    }
  }

  it should "return a valid String from a list of fragments" in new ClockFixture {
    CustomIndexName(
      Vector(DateTimeFragment("YYYY-MM-dd", TestClock),
        TextFragment("ABC"),
        DateTimeFragment("HH:mm:ss", TestClock))
    ).toString shouldBe "2016-10-02ABC14:00:00"
  }
}
