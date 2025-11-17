import type { Ref } from 'vue'

interface UploadResponse {
  key: string
  url: string
  message: string
}

interface PresignedUploadResponse {
  uploadUrl: string
  expiresIn: string
}

export const useS3Upload = () => {
  const config = useRuntimeConfig()
  const toast = useToast()

  const isUploading: Ref<boolean> = ref(false)
  const uploadProgress: Ref<number> = ref(0)

  /**
   * Upload a file to S3 via backend API
   * @param file - The file to upload
   * @param folder - The S3 folder (e.g., "tours", "profiles")
   * @param onProgress - Optional callback for upload progress
   * @returns The uploaded file URL and key
   */
  const uploadFile = async (
    file: File,
    folder: string = 'general',
    onProgress?: (progress: number) => void
  ): Promise<UploadResponse | null> => {
    if (!file) {
      toast.add({
        title: 'Error',
        description: 'No file selected',
        color: 'error'
      })
      return null
    }

    // Validate file type (images only)
    if (!file.type.startsWith('image/')) {
      toast.add({
        title: 'Error',
        description: 'Only image files are allowed',
        color: 'error'
      })
      return null
    }

    // Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      toast.add({
        title: 'Error',
        description: 'File size must not exceed 10MB',
        color: 'error'
      })
      return null
    }

    // Call progress callback if provided
    if (onProgress) {
      onProgress(10)
    }

    const formData = new FormData()
    formData.append('file', file)
    formData.append('folder', folder)

    try {
      isUploading.value = true
      uploadProgress.value = 0

      if (onProgress) {
        onProgress(20)
      }

      const response = await $fetch<UploadResponse>(
        `${config.public.apiBase}/api/storage/upload`,
        {
          method: 'POST',
          body: formData,
          onRequest({ options }) {
            // Add auth token from localStorage
            const token = import.meta.client ? localStorage.getItem('auth_token') : null
            if (token) {
              const existingHeaders = options.headers as any || {}
              options.headers = {
                ...(typeof existingHeaders === 'object' ? existingHeaders : {}),
                Authorization: `Bearer ${token}`
              } as any
            }
          }
          // Note: $fetch doesn't support onUploadProgress
          // onUploadProgress(event: any) {
          //   if (event.total) {
          //     uploadProgress.value = Math.round((event.loaded / event.total) * 100)
          //   }
          // }
        }
      )

      if (onProgress) {
        onProgress(100)
      }

      toast.add({
        title: 'Success',
        description: 'File uploaded successfully',
        color: 'success'
      })

      return response
    } catch (error: any) {
      console.error('Upload error:', error)
      toast.add({
        title: 'Upload Failed',
        description: error.data?.error || 'Failed to upload file',
        color: 'error'
      })
      return null
    } finally {
      isUploading.value = false
      uploadProgress.value = 0
    }
  }

  /**
   * Get a presigned upload URL for direct client upload to S3
   * @param filename - The name of the file
   * @param folder - The S3 folder
   * @returns The presigned upload URL
   */
  const getPresignedUploadUrl = async (
    filename: string,
    folder: string = 'general'
  ): Promise<string | null> => {
    try {
      const token = import.meta.client ? localStorage.getItem('auth_token') : null
      const response = await $fetch<PresignedUploadResponse>(
        `${config.public.apiBase}/api/storage/presigned-upload-url`,
        {
          method: 'GET',
          query: { filename, folder },
          headers: token
            ? {
                Authorization: `Bearer ${token}`
              }
            : {}
        }
      )

      return response.uploadUrl
    } catch (error: any) {
      console.error('Error getting presigned URL:', error)
      toast.add({
        title: 'Error',
        description: 'Failed to get upload URL',
        color: 'error'
      })
      return null
    }
  }

  /**
   * Upload directly to S3 using a presigned URL
   * @param file - The file to upload
   * @param presignedUrl - The presigned upload URL
   */
  const uploadToPresignedUrl = async (
    file: File,
    presignedUrl: string
  ): Promise<boolean> => {
    try {
      isUploading.value = true
      uploadProgress.value = 0

      await $fetch(presignedUrl, {
        method: 'PUT',
        body: file,
        headers: {
          'Content-Type': file.type
        }
        // Note: $fetch doesn't support onUploadProgress
        // onUploadProgress(event: any) {
        //   if (event.total) {
        //     uploadProgress.value = Math.round((event.loaded / event.total) * 100)
        //   }
        // }
      })

      toast.add({
        title: 'Success',
        description: 'File uploaded successfully',
        color: 'success'
      })

      return true
    } catch (error) {
      console.error('Direct upload error:', error)
      toast.add({
        title: 'Upload Failed',
        description: 'Failed to upload file to S3',
        color: 'error'
      })
      return false
    } finally {
      isUploading.value = false
      uploadProgress.value = 0
    }
  }

  /**
   * Delete a file from S3
   * @param key - The S3 object key (folder/filename)
   */
  const deleteFile = async (key: string): Promise<boolean> => {
    try {
      const token = import.meta.client ? localStorage.getItem('auth_token') : null
      const [folder, filename] = key.split('/')

      await $fetch(`${config.public.apiBase}/api/storage/${folder}/${filename}`, {
        method: 'DELETE',
        headers: token
          ? {
              Authorization: `Bearer ${token}`
            }
          : {}
      })

      toast.add({
        title: 'Success',
        description: 'File deleted successfully',
        color: 'success'
      })

      return true
    } catch (error) {
      console.error('Delete error:', error)
      toast.add({
        title: 'Delete Failed',
        description: 'Failed to delete file',
        color: 'error'
      })
      return false
    }
  }

  /**
   * Get public URL for an S3 object
   * @param key - The S3 object key
   */
  const getPublicUrl = async (key: string): Promise<string | null> => {
    try {
      const response = await $fetch<{ url: string }>(
        `${config.public.apiBase}/api/storage/url`,
        {
          method: 'GET',
          query: { key }
        }
      )

      return response.url
    } catch (error) {
      console.error('Error getting public URL:', error)
      return null
    }
  }

  return {
    isUploading: readonly(isUploading),
    uploadProgress: readonly(uploadProgress),
    uploadFile,
    getPresignedUploadUrl,
    uploadToPresignedUrl,
    deleteFile,
    getPublicUrl
  }
}
