import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './styles.css'; // Import custom CSS
import { BASE_URL } from './config';



//const BASE_URL = 'https://airport-service.cfapps.us10-001.hana.ondemand.com/api/airports';

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
    // Convert elevation, lat, lon to appropriate types
    const payload = {
      ...newAirport,
      elevation: parseInt(newAirport.elevation) || 0,
      lat: parseFloat(newAirport.lat) || 0.0,
      lon: parseFloat(newAirport.lon) || 0.0,
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
            {Object.entries(newAirport).map(([key, value]) => (
              <div key={key}>
                <label>{key.toUpperCase()}</label>
                <input
                  type="text"
                  value={value}
                  onChange={(e) => setNewAirport({ ...newAirport, [key]: e.target.value })}
                />
              </div>
            ))}
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
