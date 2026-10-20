export const maxImageBytes = 5 * 1024 * 1024;

const jpegSignature = [0xff, 0xd8, 0xff];
const pngSignature = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a];

function startsWith(bytes: Uint8Array, signature: number[]): boolean {
  return signature.every((value, index) => bytes[index] === value);
}

/** Checks the real content (first bytes) of the file, not only its name or declared type. */
export async function validateImageFile(file: File): Promise<string | null> {
  if (file.size === 0) {
    return "La imagen está vacía";
  }
  if (file.size > maxImageBytes) {
    return "La imagen supera el tamaño máximo de 5 MB";
  }
  const header = new Uint8Array(await file.slice(0, pngSignature.length).arrayBuffer());
  if (!startsWith(header, jpegSignature) && !startsWith(header, pngSignature)) {
    return "El archivo no es una imagen JPG o PNG válida";
  }
  return null;
}
