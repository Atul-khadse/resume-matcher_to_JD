import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Upload, FileText, CheckCircle, RefreshCw, Briefcase, Award } from 'lucide-react';

export default function CandidateDashboard() {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState(null);
  const [existingResume, setExistingResume] = useState(null);
  const [scores, setScores] = useState([]);
  const [loadingScores, setLoadingScores] = useState(false);

  const fetchMyResume = async () => {
    try {
      const res = await client.get('/api/resumes/my');
      setExistingResume(res.data);
    } catch (err) {
      // Resume not uploaded yet
    }
  };

  const fetchMyScores = async () => {
    setLoadingScores(true);
    try {
      const res = await client.get('/api/matches/my-scores');
      setScores(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingScores(false);
    }
  };

  useEffect(() => {
    fetchMyResume();
    fetchMyScores();
  }, []);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) return;

    setUploading(true);
    setUploadMessage(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const res = await client.post('/api/resumes/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setUploadMessage({ type: 'success', text: res.data.message });
      setFile(null);
      await fetchMyResume();
      // Scoring runs asynchronously in matching-service, wait 1.5s then fetch updated scores
      setTimeout(() => {
        fetchMyScores();
      }, 1500);
    } catch (err) {
      setUploadMessage({
        type: 'error',
        text: err.response?.data?.error || 'Failed to upload resume. Ensure it is a valid PDF.',
      });
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-900">Candidate Dashboard</h1>
        <p className="text-sm text-slate-500">Upload your PDF resume to compute match scores against all posted jobs</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Upload Box */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 h-fit">
          <h2 className="text-base font-bold text-slate-800 mb-4">Resume Upload</h2>

          {uploadMessage && (
            <div
              className={`p-3 text-xs rounded-lg mb-4 ${
                uploadMessage.type === 'success'
                  ? 'bg-emerald-50 border border-emerald-200 text-emerald-800'
                  : 'bg-red-50 border border-red-200 text-red-800'
              }`}
            >
              {uploadMessage.text}
            </div>
          )}

          <form onSubmit={handleUpload} className="space-y-4">
            <div className="border-2 border-dashed border-slate-200 rounded-xl p-6 text-center hover:border-indigo-400 transition">
              <Upload className="w-8 h-8 text-slate-400 mx-auto mb-2" />
              <p className="text-xs text-slate-600 mb-1">Upload PDF Resume (Max 10MB)</p>
              <input
                type="file"
                accept=".pdf"
                onChange={(e) => setFile(e.target.files[0])}
                className="text-xs text-slate-500 file:mr-2 file:py-1 file:px-2 file:rounded file:border-0 file:text-xs file:bg-slate-100 file:text-slate-700 hover:file:bg-slate-200 cursor-pointer"
              />
            </div>

            {file && (
              <p className="text-xs text-indigo-600 font-medium">Selected: {file.name}</p>
            )}

            <button
              type="submit"
              disabled={!file || uploading}
              className="w-full py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium transition disabled:opacity-50"
            >
              {uploading ? 'Extracting & Scoring...' : 'Upload & Match'}
            </button>
          </form>

          {existingResume && (
            <div className="mt-6 pt-6 border-t border-slate-100">
              <p className="text-xs font-bold uppercase text-slate-400 tracking-wider mb-2">Active Resume</p>
              <div className="flex items-center space-x-2 text-xs text-slate-700 bg-slate-50 p-2.5 rounded-lg">
                <FileText className="w-4 h-4 text-indigo-600" />
                <span className="truncate">{existingResume.fileName}</span>
              </div>
            </div>
          )}
        </div>

        {/* Matches List */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-xl border border-slate-200 p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-base font-bold text-slate-800 flex items-center space-x-2">
                <Briefcase className="w-4 h-4 text-indigo-600" />
                <span>Job Compatibility Rankings</span>
              </h2>
              <button
                onClick={fetchMyScores}
                className="flex items-center space-x-1 text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1.5 rounded-md font-medium transition"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loadingScores ? 'animate-spin' : ''}`} />
                <span>Refresh</span>
              </button>
            </div>

            {scores.length === 0 ? (
              <div className="text-center py-12 border border-dashed rounded-lg">
                <p className="text-sm text-slate-500">No match scores available yet.</p>
                <p className="text-xs text-slate-400 mt-1">Upload your resume to see where you rank across open listings.</p>
              </div>
            ) : (
              <div className="space-y-4">
                {scores.map((item) => (
                  <div key={item.id} className="border border-slate-200 rounded-lg p-4 flex items-start justify-between hover:border-slate-300 transition">
                    <div>
                      <h3 className="font-semibold text-slate-900 text-sm">{item.job?.title}</h3>
                      <p className="text-xs text-slate-500">{item.job?.company}</p>
                      {item.matchedKeywords && (
                        <div className="pt-2 flex flex-wrap gap-1">
                          {item.matchedKeywords.split(',').map((kw, i) => (
                            <span key={i} className="text-[10px] bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded font-medium">
                              {kw.trim()}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>

                    <div className="text-right">
                      <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                        <Award className="w-3.5 h-3.5 mr-1" />
                        {item.scorePercentage}%
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}