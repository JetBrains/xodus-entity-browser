import fileSize from 'filesize';

export function formatFileSize(size: number, exponent = 2): string {
  if (!size) {
    return '-';
  }
  return fileSize(size, {exponent}).toString();
}
