/*
 * Copyright 2020-2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats.effect
package laws

import cats.effect.kernel.testkit.{pure, OutcomeGenerators, PureConcGenerators, TimeT}, pure._
import cats.effect.kernel.testkit.TimeT._
import cats.laws.discipline.arbitrary._
import org.scalacheck.Prop
import org.specs2.mutable._
import org.typelevel.discipline.specs2.mutable.Discipline

import scala.concurrent.duration._

class PureConcSpec extends Specification with Discipline with BaseSpec {
  import PureConcGenerators._
  import OutcomeGenerators._

  implicit def exec(fb: TimeT[PureConc[Int, *], Boolean]): Prop =
    Prop(pure.run(TimeT.run(fb)).fold(false, _ => false, _.getOrElse(false)))

  "parallel utilities" should {
    import cats.effect.kernel.{GenConcurrent, Outcome}
    import cats.effect.kernel.implicits._
    import cats.syntax.all._

    type F[A] = PureConc[Int, A]
    val F = GenConcurrent[F]

    "short-circuit on error" in {
      pure.run((F.never[Unit], F.raiseError[Unit](42)).parTupled) mustEqual Outcome.Errored(42)
      pure.run((F.raiseError[Unit](42), F.never[Unit]).parTupled) mustEqual Outcome.Errored(42)
    }
  }

  checkAll(
    "TimeT[PureConc]",
    GenTemporalTests[TimeT[PureConc[Int, *], *], Int].temporal[Int, Int, Int](10.millis)
  )
}
