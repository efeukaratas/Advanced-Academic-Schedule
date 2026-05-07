import { useState, useEffect } from 'react';

const API = 'http://localhost:8080/api';

export default function EnrollmentsPage() {
  const [enrollments, setEnrollments] = useState([]);
  const [students,    setStudents]    = useState([]);
  const [courses,     setCourses]     = useState([]);
  const [editing,     setEditing]     = useState(null);
  const [form,        setForm]        = useState({ studentId:'', courseId:'', priority:1, approved:false });

  const load = () => {
    fetch(`${API}/enrollments`).then(r => r.json()).then(setEnrollments).catch(() => {});
    fetch(`${API}/students`).then(r => r.json()).then(setStudents).catch(() => {});
    fetch(`${API}/courses`).then(r => r.json()).then(setCourses).catch(() => {});
  };

  useEffect(load, []);

  const reset = () => { setEditing(null); setForm({ studentId:'', courseId:'', priority:1, approved:false }); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.studentId || !form.courseId) return;
    const body = { studentId: Number(form.studentId), courseId: Number(form.courseId), priority: form.priority, approved: form.approved };
    const url = editing ? `${API}/enrollments/${editing}` : `${API}/enrollments`;
    const res = await fetch(url, { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify(body) });
    if (!res.ok) { const err = await res.text(); alert(err); return; }
    reset(); load();
  };

  const handleEdit = (er) => {
    setEditing(er.id);
    setForm({ studentId: er.student?.id || '', courseId: er.course?.id || '', priority: er.priority, approved: er.approved });
  };

  const handleApprove = async (id) => {
    await fetch(`${API}/enrollments/${id}/approve`, { method:'PUT' });
    load();
  };

  const handleDelete = async (id) => {
    if (!confirm('Bu kaydi silmek istediginize emin misiniz?')) return;
    await fetch(`${API}/enrollments/${id}`, { method:'DELETE' });
    load();
  };

  const approvedCount   = enrollments.filter(e => e.approved).length;
  const pendingCount    = enrollments.filter(e => !e.approved).length;

  return (
    <div className="crud-page">
      <div className="crud-header">
        <h2>Ders Kayit Yonetimi</h2>
        <div style={{display:'flex',gap:8}}>
          <span className="crud-count">{enrollments.length} kayit</span>
          <span className="crud-count" style={{borderColor:'#10b981'}}>{approvedCount} onayli</span>
          <span className="crud-count" style={{borderColor:'#f59e0b'}}>{pendingCount} bekliyor</span>
        </div>
      </div>

      <form className="crud-form" onSubmit={handleSubmit}>
        <select value={form.studentId} onChange={e => setForm({...form, studentId:e.target.value})} required>
          <option value="">-- Ogrenci Sec --</option>
          {students.map(s => <option key={s.id} value={s.id}>{s.name} ({s.department?.name || '--'})</option>)}
        </select>
        <select value={form.courseId} onChange={e => setForm({...form, courseId:e.target.value})} required>
          <option value="">-- Ders Sec --</option>
          {courses.map(c => <option key={c.id} value={c.id}>{c.code} - {c.name}</option>)}
        </select>
        <select value={form.priority} onChange={e => setForm({...form, priority:+e.target.value})}>
          <option value={1}>Oncelik: 1 (Yuksek)</option>
          <option value={2}>Oncelik: 2 (Orta)</option>
          <option value={3}>Oncelik: 3 (Dusuk)</option>
        </select>
        <label className="checkbox-label">
          <input type="checkbox" checked={form.approved} onChange={e => setForm({...form, approved:e.target.checked})} />
          Onayli
        </label>
        <div className="crud-form-actions">
          <button type="submit" className="btn btn-primary">{editing ? 'Guncelle' : 'Kayit Ekle'}</button>
          {editing && <button type="button" className="btn btn-secondary" onClick={reset}>Iptal</button>}
        </div>
      </form>

      <div className="crud-table-wrap">
        <table className="crud-table">
          <thead>
            <tr>
              <th>ID</th><th>Ogrenci</th><th>Ders</th><th>Oncelik</th><th>Durum</th><th>Islem</th>
            </tr>
          </thead>
          <tbody>
            {enrollments.map(er => (
              <tr key={er.id}>
                <td>{er.id}</td>
                <td>{er.student?.name || '--'}</td>
                <td><span className="code-badge">{er.course?.code || '--'}</span> {er.course?.name || ''}</td>
                <td>
                  <span className={`priority-badge priority-${er.priority}`}>
                    {er.priority === 1 ? 'Yuksek' : er.priority === 2 ? 'Orta' : 'Dusuk'}
                  </span>
                </td>
                <td>
                  {er.approved
                    ? <span className="status-badge status-approved">Onayli</span>
                    : <span className="status-badge status-pending">Bekliyor</span>
                  }
                </td>
                <td className="crud-actions">
                  {!er.approved && <button className="btn-icon btn-approve" onClick={() => handleApprove(er.id)}>Onayla</button>}
                  <button className="btn-icon" onClick={() => handleEdit(er)}>Duzenle</button>
                  <button className="btn-icon btn-danger" onClick={() => handleDelete(er.id)}>Sil</button>
                </td>
              </tr>
            ))}
            {enrollments.length === 0 && <tr><td colSpan={6} style={{textAlign:'center',color:'#64748b'}}>Henuz kayit yok</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
