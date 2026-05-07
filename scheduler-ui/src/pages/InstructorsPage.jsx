import { useState, useEffect } from 'react';

const API = 'http://localhost:8080/api';

export default function InstructorsPage() {
  const [instructors,  setInstructors]  = useState([]);
  const [departments,  setDepartments]  = useState([]);
  const [editing,      setEditing]      = useState(null);
  const [form,         setForm]         = useState({ name:'', title:'', email:'', departmentId:'' });

  const load = () => {
    fetch(`${API}/instructors`).then(r => r.json()).then(setInstructors).catch(() => {});
    fetch(`${API}/schedule/departments`).then(r => r.json()).then(setDepartments).catch(() => {});
  };

  useEffect(load, []);

  const reset = () => { setEditing(null); setForm({ name:'', title:'', email:'', departmentId:'' }); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const body = { ...form, departmentId: form.departmentId ? Number(form.departmentId) : null };
    const url = editing ? `${API}/instructors/${editing}` : `${API}/instructors`;
    await fetch(url, { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify(body) });
    reset(); load();
  };

  const handleEdit = (i) => {
    setEditing(i.id);
    setForm({ name: i.name, title: i.title || '', email: i.email || '', departmentId: i.department?.id || '' });
  };

  const handleDelete = async (id) => {
    if (!confirm('Bu egitmeni silmek istediginize emin misiniz?')) return;
    await fetch(`${API}/instructors/${id}`, { method:'DELETE' });
    load();
  };

  return (
    <div className="crud-page">
      <div className="crud-header">
        <h2>Egitmen Yonetimi</h2>
        <span className="crud-count">{instructors.length} egitmen</span>
      </div>

      <form className="crud-form" onSubmit={handleSubmit}>
        <input placeholder="Ad Soyad" value={form.name} onChange={e => setForm({...form, name:e.target.value})} required />
        <select value={form.title} onChange={e => setForm({...form, title:e.target.value})}>
          <option value="">-- Unvan --</option>
          <option value="Prof. Dr.">Prof. Dr.</option>
          <option value="Doc. Dr.">Doc. Dr.</option>
          <option value="Dr.">Dr.</option>
          <option value="Ogr. Gor.">Ogr. Gor.</option>
          <option value="Ars. Gor.">Ars. Gor.</option>
        </select>
        <input placeholder="E-posta" type="email" value={form.email} onChange={e => setForm({...form, email:e.target.value})} />
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
              <th>ID</th><th>Unvan</th><th>Ad Soyad</th><th>E-posta</th><th>Departman</th><th>Islem</th>
            </tr>
          </thead>
          <tbody>
            {instructors.map(i => (
              <tr key={i.id}>
                <td>{i.id}</td>
                <td><span className="title-badge">{i.title || '--'}</span></td>
                <td>{i.name}</td>
                <td>{i.email || '--'}</td>
                <td>{i.department?.name || '--'}</td>
                <td className="crud-actions">
                  <button className="btn-icon" onClick={() => handleEdit(i)} title="Duzenle">Duzenle</button>
                  <button className="btn-icon btn-danger" onClick={() => handleDelete(i.id)} title="Sil">Sil</button>
                </td>
              </tr>
            ))}
            {instructors.length === 0 && <tr><td colSpan={6} style={{textAlign:'center',color:'#64748b'}}>Henuz egitmen yok</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
