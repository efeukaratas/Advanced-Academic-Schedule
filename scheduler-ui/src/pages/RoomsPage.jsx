import { useState, useEffect } from 'react';

const API = 'http://localhost:8080/api';

export default function RoomsPage() {
  const [rooms,    setRooms]    = useState([]);
  const [editing,  setEditing]  = useState(null);
  const [form,     setForm]     = useState({ name:'', building:'', capacity:30, roomType:'Sinif' });

  const load = () => {
    fetch(`${API}/rooms`).then(r => r.json()).then(setRooms).catch(() => {});
  };

  useEffect(load, []);

  const reset = () => { setEditing(null); setForm({ name:'', building:'', capacity:30, roomType:'Sinif' }); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const url = editing ? `${API}/rooms/${editing}` : `${API}/rooms`;
    await fetch(url, { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify(form) });
    reset(); load();
  };

  const handleEdit = (r) => {
    setEditing(r.id);
    setForm({ name: r.name, building: r.building || '', capacity: r.capacity, roomType: r.roomType || 'Sinif' });
  };

  const handleDelete = async (id) => {
    if (!confirm('Bu odayi silmek istediginize emin misiniz?')) return;
    await fetch(`${API}/rooms/${id}`, { method:'DELETE' });
    load();
  };

  return (
    <div className="crud-page">
      <div className="crud-header">
        <h2>Oda Yonetimi</h2>
        <span className="crud-count">{rooms.length} oda</span>
      </div>

      <form className="crud-form" onSubmit={handleSubmit}>
        <input placeholder="Oda Adi (A-101)" value={form.name} onChange={e => setForm({...form, name:e.target.value})} required />
        <input placeholder="Bina (A Blok)" value={form.building} onChange={e => setForm({...form, building:e.target.value})} />
        <input type="number" placeholder="Kapasite" value={form.capacity} onChange={e => setForm({...form, capacity:+e.target.value})} min={5} />
        <select value={form.roomType} onChange={e => setForm({...form, roomType:e.target.value})}>
          <option value="Sinif">Sinif</option>
          <option value="Lab">Laboratuvar</option>
          <option value="Amfi">Amfi</option>
          <option value="Toplanti">Toplanti</option>
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
              <th>ID</th><th>Oda Adi</th><th>Bina</th><th>Kapasite</th><th>Tip</th><th>Islem</th>
            </tr>
          </thead>
          <tbody>
            {rooms.map(r => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td><span className="code-badge">{r.name}</span></td>
                <td>{r.building || '--'}</td>
                <td><strong>{r.capacity}</strong> kisi</td>
                <td>{r.roomType || '--'}</td>
                <td className="crud-actions">
                  <button className="btn-icon" onClick={() => handleEdit(r)} title="Duzenle">Duzenle</button>
                  <button className="btn-icon btn-danger" onClick={() => handleDelete(r.id)} title="Sil">Sil</button>
                </td>
              </tr>
            ))}
            {rooms.length === 0 && <tr><td colSpan={6} style={{textAlign:'center',color:'#64748b'}}>Henuz oda yok</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
