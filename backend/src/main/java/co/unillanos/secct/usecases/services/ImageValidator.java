package co.unillanos.secct.usecases.services;

import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.springframework.stereotype.Component;

/** Validates the real content of an uploaded image (magic bytes), not its name or declared type. */
@Component
public class ImageValidator {

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public void validateFormat(byte[] image) {
        if (image == null || image.length == 0) {
            throw new BusinessException("La imagen está vacía", 400);
        }
        if (!startsWith(image, JPEG_SIGNATURE) && !startsWith(image, PNG_SIGNATURE)) {
            throw new BusinessException("El archivo no es una imagen JPG o PNG válida", 400);
        }
    }

    public void validateSize(byte[] image, long maxBytes) {
        if (image != null && image.length > maxBytes) {
            throw new BusinessException(
                    "La imagen supera el tamaño máximo de " + (maxBytes / (1024 * 1024)) + " MB", 413);
        }
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
