import { useState, useEffect } from 'react';

const API = 'http://localhost:8080/api';

export default function CoursesPage() {
  const [courses,      setCourses]      = useState([]);
  const [departments,  setDepartments]  = useState([]);
  const [instructors,  setInstructors]  = useState([]);
  const [editing,      setEditing]      = useState(null);
  const [form,         setForm]         = useState({ name:'', code:'', credits:3, minRoomCapacity:25, instructorId:'', departmentId:'' });

  const load = () => {
    fetch(`${API}/courses`).then(r => r.json()).then(setCourses).catch(() => {});
    fetch(`${API}/instructors`).then(r => r.json()).then(setInstructors).catch(() => {});
    fetch(`${API}/schedule/departments`).then(r => r.json()).then(setDepartments).catch(() => {});
  };

  useEffect(load, []);

  const reset = () => { setEditing(null); setForm({ name:'', code:'', credits:3, minRoomCapacity:25, instructorId:'', departmentId:'' }); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const body = { ...form, instructorId: form.instructorId ? Number(form.instructorId) : null, departmentId: form.departmentId ? Number(form.departmentId) : null };
    const url = editing ? `${API}/courses/${editing}` : `${API}/courses`;
    const method = editing ? 'PUT' : 'POST';
    await fetch(url, { method, headers: { 'Content-Type':'application/json' }, body: JSON.stringify(body) });
    reset(); load();
  };

  const handleEdit = (c) => {
    setEditing(c.id);
    setForm({ name: c.name, code: c.code, credits: c.credits, minRoomCapacity: c.minRoomCapacity, instructorId: c.instructor?.id || '', departmentId: c.department?.id || '' });
  };

  const handleDelete = async (id) => {
    if (!confirm('Bu dersi silmek istediginize emin misiniz?')) return;
    await fetch(`${API}/courses/${id}`, { method:'DELETE' });
    load();
  };

  return (
    <div className="crud-page">
      <div className="crud-header">
        <h2>Ders Yonetimi</h2>
        <span className="crud-count">{courses.length} ders</span>
      </div>

      <form className="crud-form" onSubmit={handleSubmit}>
        <input placeholder="Ders Adi" value={form.name} onChange={e => setForm({...form, name:e.target.value})} required />
        <input placeholder="Kod (CS201)" value={form.code} onChange={e => setForm({...form, code:e.target.value})} required />
        <input type="number" placeholder="Kredi" value={form.credits} onChange={e => setForm({...form, credits:+e.target.value})} min={1} max={6} />
        <input type="number" placeholder="Min Kapasite" value={form.minRoomCapacity} onChange={e => setForm({...form, minRoomCapacity:+e.target.value})} min={10} />
        <select value={form.instructorId} onChange={e => setForm({...form, instructorId:e.target.value})}>
          <option value="">-- Egitmen --</option>
          {instructors.map(i => <option key={i.id} value={i.id}>{i.title} {i.name}</option>)}
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
              <th>ID</th><th>Kod</th><th>Ders Adi</th><th>Kredi</th><th>Egitmen</th><th>Departman</th><th>Min Kap.</th><th>Islem</th>
            </tr>
          </thead>
          <tbody>
            {courses.map(c => (
              <tr key={c.id}>
                <td>{c.id}</td>
                <td><span className="code-badge">{c.code}</span></td>
                <td>{c.name}</td>
                <td>{c.credits}</td>
                <td>{c.instructor ? `${c.instructor.title || ''} ${c.instructor.name}` : '--'}</td>
                <td>{c.department?.name || '--'}</td>
                <td>{c.minRoomCapacity}</td>
                <td className="crud-actions">
                  <button className="btn-icon" onClick={() => handleEdit(c)} title="Duzenle">Duzenle</button>
                  <button className="btn-icon btn-danger" onClick={() => handleDelete(c.id)} title="Sil">Sil</button>
                </td>
              </tr>
            ))}
            {courses.length === 0 && <tr><td colSpan={8} style={{textAlign:'center',color:'#64748b'}}>Henuz ders yok</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
