import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './styles.css'; // Import custom CSS
import { BASE_URL } from './config';

function App() {
  const [airports, setAirports] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [searchName, setSearchName] = useState('');
  const [sortField, setSortField] = useState('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [newAirport, setNewAirport] = useState({
    icao: '',
    iata: '',
    name: '',
    city: '',
    state: '',
    country: '',
    tz: '',
    elevation: '',
    lat: '',
    lon: ''
  });
  const [addError, setAddError] = useState('');

  // Fields that are allowed for sorting
  const sortableFields = ['name', 'city', 'state', 'country'];
  const navigate = useNavigate();

  // Fetch airports
  useEffect(() => {
    fetchAirports();
  }, [page, sortField]);

  const fetchAirports = () => {
    setLoading(true);
    axios
      .get(`${BASE_URL}/page?page=${page}&size=${pageSize}&sortBy=${sortField}`)
      .then((response) => {
        const fetchedAirports = response.data.content;
        setAirports(fetchedAirports);
        setTotalPages(response.data.totalPages);
        setLoading(false);
      })
      .catch((error) => {
        console.error('Error fetching airport data:', error);
        setLoading(false);
      });
  };

  const handleSearch = () => {
    if (!searchName.trim()) {
      fetchAirports();
      return;
    }
    setLoading(true);
    axios
      .get(`${BASE_URL}/filter-by-name?name=${searchName}`)
      .then((response) => {
        setAirports(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error('Error searching airport:', error);
        setLoading(false);
      });
  };

  const handleSort = (field) => {
    // Apply sorting only on the allowed fields
    if (sortableFields.includes(field)) {
      setSortField(field);
    }
  };

  const handleAddAirport = () => {
    setAddError('');
    // Convert elevation, lat, lon to numbers, no defaults
    const payload = {
      ...newAirport,
      elevation: newAirport.elevation ? parseInt(newAirport.elevation) : null,
      lat: newAirport.lat ? parseFloat(newAirport.lat) : null,
      lon: newAirport.lon ? parseFloat(newAirport.lon) : null
    };

    axios.post(BASE_URL, payload)
      .then(() => {
        setShowAddForm(false);
        setNewAirport({
          icao: '',
          iata: '',
          name: '',
          city: '',
          state: '',
          country: '',
          tz: '',
          elevation: '',
          lat: '',
          lon: ''
        });
        fetchAirports(); // Refresh the list
      })
      .catch((error) => {
        const msg = error.response?.data?.message || error.message;
        setAddError(msg);
      });
  };

  return (
    <div className="container">
      <h1>Airport Dashboard</h1>

      <div className="top-buttons">
        <button className="analytics-btn" onClick={() => navigate('/analytics')}>
          Go to Analytics
        </button>
      </div>

      {/* Search Bar */}
      <div className="search-bar">
        <div className="search-input-wrapper">
          <i className="fas fa-search search-icon"></i>
          <input
            type="text"
            placeholder="Search airports by name..."
            value={searchName}
            onChange={(e) => setSearchName(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          />
        </div>
        <button onClick={handleSearch}>Search</button>
        <button onClick={() => setShowAddForm(true)}>Add Airport</button>
      </div>

      {showAddForm && (
        <div className="add-airport-form">
          <h3>Add New Airport</h3>
          <div className="form-grid">
            <div>
              <label>ICAO</label>
              <input
                type="text"
                value={newAirport.icao}
                onChange={(e) => setNewAirport({ ...newAirport, icao: e.target.value })}
                required
                pattern="[A-Z0-9]{4}"
                title="ICAO must be exactly 4 uppercase letters or numbers"
              />
            </div>
            <div>
              <label>IATA</label>
              <input
                type="text"
                value={newAirport.iata}
                onChange={(e) => setNewAirport({ ...newAirport, iata: e.target.value })}
              />
            </div>
            <div>
              <label>Name</label>
              <input
                type="text"
                value={newAirport.name}
                onChange={(e) => setNewAirport({ ...newAirport, name: e.target.value })}
                required
                title="Name is required and cannot be empty"
              />
            </div>
            <div>
              <label>City</label>
              <input
                type="text"
                value={newAirport.city}
                onChange={(e) => setNewAirport({ ...newAirport, city: e.target.value })}
              />
            </div>
            <div>
              <label>State</label>
              <input
                type="text"
                value={newAirport.state}
                onChange={(e) => setNewAirport({ ...newAirport, state: e.target.value })}
              />
            </div>
            <div>
              <label>Country</label>
              <input
                type="text"
                value={newAirport.country}
                onChange={(e) => setNewAirport({ ...newAirport, country: e.target.value })}
                required
                pattern="[A-Z]{2}"
                title="Country code must be two uppercase letters (e.g., US)"
              />
            </div>
            <div>
              <label>Timezone</label>
              <input
                type="text"
                value={newAirport.tz}
                onChange={(e) => setNewAirport({ ...newAirport, tz: e.target.value })}
                required
                title="Timezone is required and cannot be empty"
              />
            </div>
            <div>
              <label>Elevation (ft)</label>
              <input
                type="number"
                value={newAirport.elevation}
                onChange={(e) => setNewAirport({ ...newAirport, elevation: e.target.value })}
                required
                step="1"
                title="Elevation must be an integer in feet"
              />
            </div>
            <div>
              <label>Latitude (degrees)</label>
              <input
                type="number"
                value={newAirport.lat}
                onChange={(e) => setNewAirport({ ...newAirport, lat: e.target.value })}
                required
                min="-90"
                max="90"
                step="any"
                title="Latitude must be between -90 and 90 degrees"
              />
            </div>
            <div>
              <label>Longitude (degrees)</label>
              <input
                type="number"
                value={newAirport.lon}
                onChange={(e) => setNewAirport({ ...newAirport, lon: e.target.value })}
                required
                min="-180"
                max="180"
                step="any"
                title="Longitude must be between -180 and 180 degrees"
              />
            </div>
          </div>
          {addError && <div className="error-msg">{addError}</div>}
          <div className="form-actions">
            <button onClick={handleAddAirport}>Submit</button>
            <button onClick={() => setShowAddForm(false)}>Cancel</button>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loader">Loading...</div>
      ) : (
        <>
          {/* Airport Table */}
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  {['icao', 'iata', 'name', 'city', 'state', 'country', 'elevation', 'lat', 'lon', 'tz', 'region'].map(
                    (field) => (
                      <th
                        key={field}
                        onClick={() => handleSort(field)}
                        className={sortableFields.includes(field) ? 'sortable' : ''}
                      >
                        {field.charAt(0).toUpperCase() + field.slice(1)}
                        {sortField === field && <i className="fas fa-sort"></i>}
                      </th>
                    )
                  )}
                </tr>
              </thead>
              <tbody>
                {airports.map((airport) => (
                  <tr
                    key={airport.icao}
                    className={airport.elevation > 8000 ? 'highlight' : ''}
                  >
                    <td>{airport.icao}</td>
                    <td>{airport.iata}</td>
                    <td>{airport.name}</td>
                    <td>{airport.city}</td>
                    <td>{airport.state}</td>
                    <td>{airport.country}</td>
                    <td>{airport.elevation}</td>
                    <td>{airport.lat}</td>
                    <td>{airport.lon}</td>
                    <td>{airport.tz}</td>
                    <td>{airport.region}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <div className="pagination">
            <button disabled={page === 0} onClick={() => setPage(page - 1)}>
              <i className="fas fa-chevron-left"></i> Previous
            </button>
            <span>
              Page {page + 1} of {totalPages}
            </span>
            <button disabled={page + 1 === totalPages} onClick={() => setPage(page + 1)}>
              Next <i className="fas fa-chevron-right"></i>
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default App;