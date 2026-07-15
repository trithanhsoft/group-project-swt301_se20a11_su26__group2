/**
 * Video URL utility functions for optimized video loading.
 *
 * Handles YouTube embed URL conversion and Cloudinary video URL optimization
 * (auto-format, auto-quality) to reduce load times significantly.
 */

/**
 * Checks if a URL points to a YouTube video.
 */
export const isYoutubeUrl = (url?: string): boolean => {
  if (!url) return false;
  return url.includes('youtube.com') || url.includes('youtu.be');
};

/**
 * Converts a YouTube URL (watch, short, embed) into an embeddable iframe URL.
 * Returns the original URL unchanged if it's not a YouTube link.
 */
export const getYoutubeEmbedUrl = (url?: string): string => {
  if (!url) return '';
  const regExp = /^.*(youtu\.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
  const match = url.match(regExp);
  if (match && match[2].length === 11) {
    return `https://www.youtube.com/embed/${match[2]}`;
  }
  return url;
};

/**
 * Checks if a URL is a Cloudinary video URL.
 * Cloudinary URLs follow the pattern: https://res.cloudinary.com/<cloud>/video/upload/...
 */
export const isCloudinaryVideoUrl = (url?: string): boolean => {
  if (!url) return false;
  return url.includes('res.cloudinary.com') && url.includes('/video/upload/');
};

/**
 * Optimizes a Cloudinary video URL by injecting transformation parameters.
 *
 * Transformations applied:
 * Transformations applied:
 * - q_auto: Automatically compresses video quality to the optimal level
 *   (typically reduces file size by 40-70% with no visible quality loss).
 *
 * Note: We explicitly DO NOT use `f_auto` because Cloudinary generates WebM
 * on-the-fly for Chrome, which breaks byte-range seeking (tua video) in native 
 * HTML5 <video> tags. Keeping it as MP4 ensures perfect seeking support.
 *
 * Example:
 *   Input:  https://res.cloudinary.com/demo/video/upload/v1234/folder/video.mp4
 *   Output: https://res.cloudinary.com/demo/video/upload/q_auto/v1234/folder/video.mp4
 *
 * If the URL already contains transformations, the optimization params are prepended.
 * Non-Cloudinary URLs are returned unchanged.
 */
export const optimizeCloudinaryVideoUrl = (url?: string): string => {
  if (!url) return '';
  if (!isCloudinaryVideoUrl(url)) return url;

  const OPTIMIZATION_PARAMS = 'q_auto';

  // Already has these transformations applied
  if (url.includes(OPTIMIZATION_PARAMS)) return url;

  // Cloudinary URL structure: .../video/upload/[existing_transforms/]v<version>/...
  // We insert our optimization params right after /upload/
  const uploadSegment = '/video/upload/';
  const uploadIndex = url.indexOf(uploadSegment);
  if (uploadIndex === -1) return url;

  const insertPosition = uploadIndex + uploadSegment.length;
  const before = url.substring(0, insertPosition);
  const after = url.substring(insertPosition);

  return `${before}${OPTIMIZATION_PARAMS}/${after}`;
};

/**
 * Returns the best video source URL for playback.
 * - YouTube links are converted to embed URLs (for use in iframes).
 * - Cloudinary links are optimized with q_auto.
 * - Other direct URLs are returned as-is.
 */
export const getOptimizedVideoUrl = (url?: string): string => {
  if (!url) return '';
  if (isYoutubeUrl(url)) return getYoutubeEmbedUrl(url);
  if (isCloudinaryVideoUrl(url)) return optimizeCloudinaryVideoUrl(url);
  return url;
};
