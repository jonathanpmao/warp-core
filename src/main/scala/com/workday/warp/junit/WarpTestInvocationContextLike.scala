package com.workday.warp.junit

import java.util

import org.junit.jupiter.api.extension.{Extension, TestTemplateInvocationContext}

import scala.collection.JavaConverters.seqAsJavaList

/**
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpTestInvocationContextLike extends TestTemplateInvocationContext {
  import WarpTestInvocationContextLike._

  // plain vanilla method display name
  val plainDisplayName: String
  // "warmup" or "trial"
  val repetitionType: String
  val currentRepetition: Int
  val totalRepetitions: Int
  val additionalExtensions: Seq[Extension]

  /**
   * Formats display name including current repetition info.
   *
   * @param invocationIndex unused.
   * @return A test invocation display name including current repetition info.
   */
  override def getDisplayName(invocationIndex: Int): String = {
    displayNamePattern
      .replace(plainDisplayNameToken, this.plainDisplayName)
      .replace(currentRepToken, String.valueOf(this.currentRepetition))
      .replace(totalRepsToken, String.valueOf(this.totalRepetitions))
      .replace(repTypeToken, this.repetitionType)
  }

  /**
    * Gets additional JUnit extensions for this invocation.
    *
    * We always use a [[WarpInfoParameterResolver]], and we use a [[MeasurementExtension]] for measured trials only.
    *
    * @return additional JUnit extensions for this test invocation.
    */
  override def getAdditionalExtensions: util.List[Extension] = {
    seqAsJavaList(WarpInfoParameterResolver(this.currentRepetition, this.totalRepetitions) :: additionalExtensions.toList)
  }
}

object WarpTestInvocationContextLike {
  /** Placeholder for the plain [[org.junit.jupiter.api.TestInfo]] display name of a [[WarpTest]] method. */
  val plainDisplayNameToken = "{plainDisplayName}"

  /** Placeholder for the current repetition count of a [[WarpTest]] method. */
  val currentRepToken = "{currentRepetition}"

  /** Placeholder for the total number of repetitions of a [[WarpTest]] method. */
  val totalRepsToken = "{totalRepetitions}"

  /** Placeholder for the type of run of a [[WarpTest]] method, eg "warmup" or "trial". */
  val repTypeToken = "{repetitionType}"

  /** Display name pattern for a [[WarpTest]]. */
  val displayNamePattern: String = s"$plainDisplayNameToken [$repTypeToken $currentRepToken of $totalRepsToken]"
}

case class WarpTestInvocationContext(plainDisplayName: String,
                                     repetitionType: String,
                                     currentRepetition: Int,
                                     totalRepetitions: Int,
                                     additionalExtensions: Seq[Extension] = Seq.empty) extends WarpTestInvocationContextLike