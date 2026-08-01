"use client";

import { useRef, useState } from "react";
import { CloudUpload, File, CheckCircle, XCircle, Download, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { uploadFile, getJobStatus } from "@/services/api";
import type { Tool } from "@/components/home/tool-card";

type UploadDropzoneProperties = {
  supportedTypes: string;
  activeTool: Tool;
};

export function UploadDropzone({ supportedTypes, activeTool }: UploadDropzoneProperties) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [progress, setProgress] = useState(0);
  
  // App states: idle -> uploading -> processing -> success | error
  const [status, setStatus] = useState<"idle" | "uploading" | "processing" | "success" | "error">("idle");
  const [message, setMessage] = useState<string>("");
  const [currentJobId, setCurrentJobId] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);



  const resetState = () => {
    setSelectedFile(null);
    setStatus("idle");
    setMessage("");
    setProgress(0);
    setCurrentJobId(null);
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      processFileSelection(e.target.files[0]);
    }
  };

  const processFileSelection = (file: File) => {
    setSelectedFile(file);
    setStatus("idle");
    setMessage("");
    setProgress(0);
    setCurrentJobId(null);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setProgress(0);
    setStatus("uploading");
    setMessage("Uploading file...");

    try {
      const response = await uploadFile(selectedFile, activeTool.targetFormat, (percentCompleted) => {
        setProgress(percentCompleted);
      });

      if (response.success && response.data?.jobId) {
        setStatus("processing");
        setMessage(`Processing: ${activeTool.title}...`);
        setCurrentJobId(response.data.jobId);
        pollJobStatus(response.data.jobId);
      } else {
        setStatus("error");
        setMessage(response.message || "Upload failed");
      }
    } catch (err: unknown) {
      setStatus("error");
      if (err instanceof Error && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setMessage(axiosError.response?.data?.message || "Upload failed");
      } else {
        setMessage("Upload failed");
      }
    }
  };

  const pollJobStatus = async (jobId: string) => {
    try {
      const response = await getJobStatus(jobId);
      if (response.success && response.data) {
        const jobStatus = response.data.conversionStatus;
        
        if (jobStatus === "COMPLETED") {
          setStatus("success");
          setMessage("Conversion successful!");
        } else if (jobStatus === "FAILED") {
          setStatus("error");
          setMessage(response.data.errorMessage || "Conversion failed");
        } else {
          // Continue polling
          setTimeout(() => pollJobStatus(jobId), 1500);
        }
      } else {
        setStatus("error");
        setMessage("Failed to retrieve job status");
      }
    } catch {
      setStatus("error");
      setMessage("Error checking job status");
    }
  };

  const handleDownload = () => {
    if (currentJobId) {
      window.location.href = `http://localhost:8080/api/v1/jobs/${currentJobId}/download`;
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  return (
    <div
      className={`relative flex h-full min-h-[400px] flex-col items-center justify-center rounded-2xl border border-dashed bg-slate-900/60 p-6 text-center sm:p-8 transition-colors ${
        isDragging ? "border-sky-400 bg-slate-800/80" : "border-slate-700"
      }`}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {/* Show active tool badge when idle */}
      {!selectedFile && (
        <div className="absolute top-6 left-1/2 -translate-x-1/2 rounded-full border border-sky-400/20 bg-sky-400/10 px-4 py-1 text-sm text-sky-300">
          Ready for {activeTool.title}
        </div>
      )}

      {!selectedFile ? (
        <div className="mt-8 flex flex-col items-center">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-sky-400/10 text-sky-300">
            <CloudUpload aria-hidden="true" className="h-8 w-8" />
          </div>
          <h3 className="mt-6 text-xl font-semibold text-white">Drop your file here</h3>
          <p className="mt-2 max-w-sm text-sm leading-6 text-slate-400">
            Drag and drop your document to convert it to {activeTool.targetFormat.toUpperCase()}, or browse your device.
          </p>
          
          <input
            type="file"
            ref={fileInputRef}
            className="hidden"
            onChange={handleFileChange}
          />
          
          <Button 
            type="button" 
            variant="outline" 
            size="lg" 
            className="mt-8 hover:bg-sky-400/10 hover:text-sky-300 hover:border-sky-400/30"
            onClick={() => fileInputRef.current?.click()}
          >
            Browse files
          </Button>
          <p className="mt-4 text-xs text-slate-500">Supported formats: {supportedTypes}</p>
        </div>
      ) : (
        <div className="flex w-full flex-col items-center max-w-md mx-auto">
          <div className={`mx-auto flex h-16 w-16 items-center justify-center rounded-2xl ${
            status === "success" ? "bg-emerald-400/10 text-emerald-400" :
            status === "error" ? "bg-rose-400/10 text-rose-400" :
            "bg-sky-400/10 text-sky-300"
          }`}>
            {status === "success" ? <CheckCircle className="h-8 w-8" /> : 
             status === "error" ? <XCircle className="h-8 w-8" /> : 
             <File className="h-8 w-8" />}
          </div>
          
          <h3 className="mt-6 max-w-full truncate px-4 text-xl font-semibold text-white">
            {selectedFile.name}
          </h3>
          <p className="mt-2 text-sm text-slate-400">{formatFileSize(selectedFile.size)}</p>

          {/* Progress Bar */}
          {(status === "uploading" || status === "processing") && (
            <div className="mt-8 w-full space-y-3">
              <div className="flex justify-between text-sm font-medium text-slate-300">
                <span>{message}</span>
                {status === "uploading" && <span>{progress}%</span>}
              </div>
              <div className="h-2 w-full overflow-hidden rounded-full bg-slate-800">
                <div 
                  className={`h-full rounded-full transition-all duration-300 ease-in-out ${
                    status === "processing" ? "bg-indigo-400 w-full animate-pulse" : "bg-sky-400"
                  }`}
                  style={{ width: status === "processing" ? "100%" : `${progress}%` }}
                />
              </div>
            </div>
          )}

          {/* Status Message */}
          {(status === "success" || status === "error") && (
            <div className={`mt-6 flex flex-col items-center gap-2 p-4 rounded-xl border ${
              status === "success" 
                ? "border-emerald-500/20 bg-emerald-500/10 text-emerald-300" 
                : "border-rose-500/20 bg-rose-500/10 text-rose-300"
            }`}>
              <div className="flex items-center gap-2 text-base font-semibold">
                {status === "success" ? <CheckCircle className="h-5 w-5" /> : <AlertCircle className="h-5 w-5" />}
                {status === "success" ? "Conversion Complete" : "Conversion Failed"}
              </div>
              <p className="text-sm text-center mt-1 opacity-90">{message}</p>
            </div>
          )}

          {/* Actions */}
          <div className="mt-8 flex w-full flex-col gap-3 sm:flex-row sm:justify-center">
            {status === "idle" && (
              <>
                <Button type="button" variant="outline" onClick={resetState}>
                  Cancel
                </Button>
                <Button type="button" onClick={handleUpload}>
                  Convert to {activeTool.targetFormat.toUpperCase()}
                </Button>
              </>
            )}
            
            {status === "success" && (
              <>
                <Button type="button" variant="outline" onClick={resetState}>
                  Convert another
                </Button>
                <Button type="button" onClick={handleDownload} className="bg-emerald-600 hover:bg-emerald-500 text-white border-transparent">
                  <Download className="mr-2 h-4 w-4" />
                  Download File
                </Button>
              </>
            )}
            
            {status === "error" && (
              <>
                <Button type="button" variant="outline" onClick={resetState}>
                  Try a different file
                </Button>
                <Button type="button" onClick={handleUpload}>
                  Retry conversion
                </Button>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
