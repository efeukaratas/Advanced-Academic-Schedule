import { useState, useEffect } from 'react';

const API = 'http://localhost:8080/api';

export default function StudentsPage() {
  const [students,    setStudents]    = useState([]);
  const [departments, setDepartments] = useState([]);
  const [editing,     setEditing]     = useState(null);
  const [form,        setForm]        = useState({ name:'', email:'', studyYear:1, departmentId:'' });

  const load = () => {
    fetch(`${API}/students`).then(r => r.json()).then(setStudents).catch(() => {});
    fetch(`${API}/schedule/departments`).then(r => r.json()).then(setDepartments).catch(() => {});
  };

  useEffect(load, []);

  const reset = () => { setEditing(null); setForm({ name:'', email:'', studyYear:1, departmentId:'' }); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const body = { ...form, departmentId: form.departmentId ? Number(form.departmentId) : null };
    const url = editing ? `${API}/students/${editing}` : `${API}/students`;
    await fetch(url, { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify(body) });
    reset(); load();
  };

  const handleEdit = (s) => {
    setEditing(s.id);
    setForm({ name: s.name, email: s.email || '', studyYear: s.studyYear, departmentId: s.department?.id || '' });
  };

  const handleDelete = async (id) => {
    if (!confirm('Bu ogrenciyi silmek istediginize emin misiniz?')) return;
    await fetch(`${API}/students/${id}`, { method:'DELETE' });
    load();
  };

  return (
    <div className="crud-page">
      <div className="crud-header">
        <h2>Ogrenci Yonetimi</h2>
        <span className="crud-count">{students.length} ogrenci</span>
      </div>

      <form className="crud-form" onSubmit={handleSubmit}>
        <input placeholder="Ad Soyad" value={form.name} onChange={e => setForm({...form, name:e.target.value})} required />
        <input placeholder="E-posta" type="email" value={form.email} onChange={e => setForm({...form, email:e.target.value})} />
        <select value={form.studyYear} onChange={e => setForm({...form, studyYear:+e.target.value})}>
          <option value={1}>1. Sinif</option>
          <option value={2}>2. Sinif</option>
          <option value={3}>3. Sinif</option>
          <option value={4}>4. Sinif</option>
        </select>
        <select value={form.departmentId} onChange={e => setForm({...form, departmentId:e.target.value})}>
          <option value="">-- Departman --</option>
          {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        <div className="crud-form-actions">
          <button type="submit" className="btn btn-primary">{editing ? 'Guncelle' : 'Ekle'}</button>
          {editing && <button type="button" className="btn btn-secondary" onClick={reset}>Iptal</button>}
        </div>
      </form>

      <div className="crud-table-wrap">
        <table className="crud-table">
          <thead>
            <tr>
              <th>ID</th><th>Ad Soyad</th><th>E-posta</th><th>Sinif</th><th>Departman</th><th>Islem</th>
            </tr>
          </thead>
          <tbody>
            {students.map(s => (
              <tr key={s.id}>
                <td>{s.id}</td>
                <td>{s.name}</td>
                <td>{s.email || '--'}</td>
                <td><span className="year-badge">{s.studyYear}. Sinif</span></td>
                <td>{s.department?.name || '--'}</td>
                <td className="crud-actions">
                  <button className="btn-icon" onClick={() => handleEdit(s)}>Duzenle</button>
                  <button className="btn-icon btn-danger" onClick={() => handleDelete(s.id)}>Sil</button>
                </td>
              </tr>
            ))}
            {students.length === 0 && <tr><td colSpan={6} style={{textAlign:'center',color:'#64748b'}}>Henuz ogrenci yok</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
