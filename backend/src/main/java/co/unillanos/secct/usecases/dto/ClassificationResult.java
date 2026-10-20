package co.unillanos.secct.usecases.dto;

import java.math.BigDecimal;

/** Result of classifying one fish part (OJO or PIEL) with the NTC 1443 category and the confidence. */
public class ClassificationResult {

    public String fishPartCode;
    public Short ntcCategory;
    public BigDecimal confidence;

    public ClassificationResult(String fishPartCode, Short ntcCategory, BigDecimal confidence) {
        this.fishPartCode = fishPartCode;
        this.ntcCategory = ntcCategory;
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "ClassificationResult{fishPartCode=" + fishPartCode + ", ntcCategory=" + ntcCategory
                + ", confidence=" + confidence + "}";
    }
}
