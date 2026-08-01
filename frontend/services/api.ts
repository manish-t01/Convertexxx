import axios, { AxiosProgressEvent } from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
});

export const uploadFile = async (
  files: File | File[],
  targetFormat: string,
  onProgress?: (progress: number) => void,
  conversionParameters?: string
) => {
  const formData = new FormData();
  
  if (Array.isArray(files)) {
    files.forEach(file => formData.append('file', file));
  } else {
    formData.append('file', files);
  }
  
  formData.append('targetFormat', targetFormat);
  if (conversionParameters) {
    formData.append('conversionParameters', conversionParameters);
  }

  const response = await apiClient.post('/upload', formData, {
    onUploadProgress: (progressEvent: AxiosProgressEvent) => {
      if (progressEvent.total && onProgress) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress(percentCompleted);
      }
    },
  });

  return response.data;
};

export const getJobStatus = async (jobId: string) => {
  const response = await apiClient.get(`/jobs/${jobId}`);
  return response.data;
};
