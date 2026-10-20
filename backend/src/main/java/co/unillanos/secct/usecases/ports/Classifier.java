package co.unillanos.secct.usecases.ports;

import co.unillanos.secct.usecases.dto.ClassificationResult;

import java.util.List;

/** Port to the external service that classifies a tilapia image according to the NTC 1443. */
public interface Classifier {

    List<ClassificationResult> classify(byte[] image);
}
