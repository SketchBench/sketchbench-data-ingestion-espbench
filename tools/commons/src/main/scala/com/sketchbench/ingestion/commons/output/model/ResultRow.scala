package com.sketchbench.ingestion.commons.output.model

abstract class ResultRow extends Result {
  val header: List[String]
  def toList(): List[String]
  def toTable(): List[List[String]]
}
