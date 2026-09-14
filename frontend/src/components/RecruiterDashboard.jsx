import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { PlusCircle, RefreshCw, Award, Users, FileText } from 'lucide-react';

export default function RecruiterDashboard() {
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [matches, setMatches] = useState([]);
  const [loadingMatches, setLoadingMatches] = useState(false);

  // Form states
  const [showModal, setShowModal] = useState(false);
  const [title, setTitle] = useState('');
  const [company, setCompany] = useState('');
  const [description, setDescription] = useState('');
  const [requirements, setRequirements] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchJobs = async () => {
    try {
      const res = await client.get('/api/jobs/my');
      setJobs(res.data);
      if (res.data.length > 0 && !selectedJob) {
        setSelectedJob(res.data[0]);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchMatches = async (jobId) => {
    setLoadingMatches(true);
    try {
      const res = await client.get(`/api/matches/job/${jobId}`);
      setMatches(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingMatches(false);
    }
  };

  useEffect(() => {
    fetchJobs();
  }, []);

  useEffect(() => {
    if (selectedJob) {
      fetchMatches(selectedJob.id);
    }
  }, [selectedJob]);

  const handleCreateJob = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const res = await client.post('/api/jobs', {
        title,
        company,
        description,
        requirements,
      });
      setShowModal(false);
      setTitle('');
      setCompany('');
      setDescription('');
      setRequirements('');
      await fetchJobs();
      setSelectedJob(res.data);
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to post job');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Recruiter Dashboard</h1>
          <p className="text-sm text-slate-500">Post job requirements and view real-time TF-IDF candidate rankings</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center space-x-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium px-4 py-2 rounded-lg transition shadow-sm text-sm"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Post New Job</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Job Postings */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 h-fit">
          <h2 className="text-base font-bold text-slate-800 mb-4 flex items-center justify-between">
            <span>Your Postings ({jobs.length})</span>
            <button onClick={fetchJobs} className="text-slate-400 hover:text-slate-600">
              <RefreshCw className="w-4 h-4" />
            </button>
          </h2>

          {jobs.length === 0 ? (
            <p className="text-sm text-slate-400 text-center py-8">No jobs posted yet.</p>
          ) : (
            <div className="space-y-2">
              {jobs.map((job) => (
                <div
                  key={job.id}
                  onClick={() => setSelectedJob(job)}
                  className={`p-3 rounded-lg border cursor-pointer transition ${
                    selectedJob?.id === job.id
                      ? 'border-indigo-600 bg-indigo-50/50'
                      : 'border-slate-200 hover:bg-slate-50'
                  }`}
                >
                  <p className="font-semibold text-slate-800 text-sm">{job.title}</p>
                  <p className="text-xs text-slate-500">{job.company}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right Column: Ranked Matches */}
        <div className="lg:col-span-2 space-y-6">
          {selectedJob ? (
            <div className="bg-white rounded-xl border border-slate-200 p-6">
              <div className="flex justify-between items-start mb-6 border-b border-slate-100 pb-4">
                <div>
                  <h2 className="text-xl font-bold text-slate-900">{selectedJob.title}</h2>
                  <p className="text-sm text-slate-500 font-medium">{selectedJob.company}</p>
                </div>
                <button
                  onClick={() => fetchMatches(selectedJob.id)}
                  className="flex items-center space-x-1 text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1.5 rounded-md font-medium transition"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${loadingMatches ? 'animate-spin' : ''}`} />
                  <span>Refresh Rankings</span>
                </button>
              </div>

              <div className="mb-6">
                <h3 className="text-xs font-bold uppercase text-slate-400 tracking-wider mb-2">Requirements</h3>
                <p className="text-xs text-slate-700 bg-slate-50 p-3 rounded-lg font-mono leading-relaxed">
                  {selectedJob.requirements}
                </p>
              </div>

              <h3 className="text-sm font-bold text-slate-800 mb-4 flex items-center space-x-2">
                <Users className="w-4 h-4 text-indigo-600" />
                <span>Ranked Candidate Matches ({matches.length})</span>
              </h3>

              {matches.length === 0 ? (
                <div className="text-center py-12 border border-dashed rounded-lg">
                  <p className="text-sm text-slate-500">No resumes scored for this job yet.</p>
                  <p className="text-xs text-slate-400 mt-1">Candidates uploading PDFs are ranked automatically via @Async scoring.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {matches.map((m, idx) => (
                    <div key={m.id} className="border border-slate-200 rounded-lg p-4 flex items-start justify-between hover:border-slate-300 transition">
                      <div className="space-y-1">
                        <div className="flex items-center space-x-2">
                          <span className="w-6 h-6 flex items-center justify-center bg-slate-100 rounded-full text-xs font-bold text-slate-600">
                            #{idx + 1}
                          </span>
                          <span className="font-semibold text-slate-900 text-sm">{m.resume?.candidateName}</span>
                          <span className="text-xs text-slate-400">({m.resume?.candidateEmail})</span>
                        </div>
                        <div className="flex items-center space-x-2 pt-1 text-xs text-slate-500">
                          <FileText className="w-3.5 h-3.5" />
                          <span>{m.resume?.fileName}</span>
                        </div>
                        {m.matchedKeywords && (
                          <div className="pt-2 flex flex-wrap gap-1">
                            {m.matchedKeywords.split(',').map((kw, i) => (
                              <span key={i} className="text-[10px] bg-slate-100 text-slate-700 px-2 py-0.5 rounded font-medium">
                                {kw.trim()}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>

                      <div className="text-right pl-4">
                        <div className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
                          <Award className="w-3 h-3 mr-1" />
                          {m.scorePercentage}% Match
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-slate-200 p-12 text-center text-slate-400">
              Select or post a job to view ranked candidates
            </div>
          )}
        </div>
      </div>

      {/* Post Job Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-lg w-full p-6 shadow-xl">
            <h3 className="text-lg font-bold text-slate-900 mb-4">Post New Job</h3>
            <form onSubmit={handleCreateJob} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Job Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Senior Java Backend Engineer"
                  className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Company</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. TechCorp Solutions"
                  className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                  value={company}
                  onChange={(e) => setCompany(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Job Description</label>
                <textarea
                  required
                  rows={3}
                  placeholder="Describe the day-to-day responsibilities..."
                  className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Requirements (Keywords)</label>
                <textarea
                  required
                  rows={3}
                  placeholder="Java, Spring Boot, Microservices, Docker, MySQL, Redis, AWS, Git..."
                  className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                  value={requirements}
                  onChange={(e) => setRequirements(e.target.value)}
                />
              </div>

              <div className="flex justify-end space-x-3 pt-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border rounded-lg text-sm text-slate-600 hover:bg-slate-50 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium disabled:opacity-50"
                >
                  {submitting ? 'Publishing...' : 'Publish Job'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
