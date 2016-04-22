/*
Copyright (C) 2016 
"JOANNEUM RESEARCH Forschungsgesellschaft mbH" 
 Graz, Austria, digital-iis@joanneum.at.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package eu.eexcess.dataquality.structure;

public class StructureRecResultAnalysisResultData {
	private double resultDistinctFracComplement;
	private double resultMedian;
	private double resultMedianPerVaildSamples;
	private double resultFracOutUpper;
	private double resultFracOutLower;
	private double resultFracOutWeighted;
	private double resultSigma;
	private int resultDistinctValues;
	private double resultCdfl05;
	private double resultCdfl075;

	public StructureRecResultAnalysisResultData() {
	}

	public double getResultDistinctFracComplement() {
		return resultDistinctFracComplement;
	}

	public void setResultDistinctFracComplement(
			double resultDistinctFracComplement) {
		this.resultDistinctFracComplement = resultDistinctFracComplement;
	}

	public double getResultMedian() {
		return resultMedian;
	}

	public void setResultMedian(double resultMedian) {
		this.resultMedian = resultMedian;
	}

	public double getResultMedianPerVaildSamples() {
		return resultMedianPerVaildSamples;
	}

	public void setResultMedianPerVaildSamples(
			double resultMedianPerVaildSamples) {
		this.resultMedianPerVaildSamples = resultMedianPerVaildSamples;
	}

	public double getResultFracOutUpper() {
		return resultFracOutUpper;
	}

	public void setResultFracOutUpper(double resultFracOutUpper) {
		this.resultFracOutUpper = resultFracOutUpper;
	}

	public double getResultFracOutLower() {
		return resultFracOutLower;
	}

	public void setResultFracOutLower(double resultFracOutLower) {
		this.resultFracOutLower = resultFracOutLower;
	}

	public double getResultFracOutWeighted() {
		return resultFracOutWeighted;
	}

	public void setResultFracOutWeighted(double resultFracOutWeighted) {
		this.resultFracOutWeighted = resultFracOutWeighted;
	}

	public double getResultSigma() {
		return resultSigma;
	}

	public void setResultSigma(double resultSigma) {
		this.resultSigma = resultSigma;
	}

	public int getResultDistinctValues() {
		return resultDistinctValues;
	}

	public void setResultDistinctValues(int resultDistinctValues) {
		this.resultDistinctValues = resultDistinctValues;
	}

	public double getResultCdfl05() {
		return resultCdfl05;
	}

	public void setResultCdfl05(double resultCdfl05) {
		this.resultCdfl05 = resultCdfl05;
	}

	public double getResultCdfl075() {
		return resultCdfl075;
	}

	public void setResultCdfl075(double resultCdfl075) {
		this.resultCdfl075 = resultCdfl075;
	}
}