import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './styles.css'; // Import custom CSS

function App() {
  const [airports, setAirports] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [searchName, setSearchName] = useState('');
  const [sortField, setSortField] = useState('');

  // State for analysis results
  const [averageElevations, setAverageElevations] = useState([]);
  const [airportsWithoutIATA, setAirportsWithoutIATA] = useState([]);
  const [mostCommonTimeZones, setMostCommonTimeZones] = useState([]);

  // Analysis functions (unchanged)
  const calculateAverageElevation = (airports) => {
    const countryElevations = {};
    airports.forEach((airport) => {
      const { country, elevation } = airport;
      if (!countryElevations[country]) {
        countryElevations[country] = { totalElevation: 0, count: 0 };
      }
      countryElevations[country].totalElevation += elevation;
      countryElevations[country].count += 1;
    });
    return Object.keys(countryElevations).map((country) => ({
      country,
      averageElevation: countryElevations[country].totalElevation / countryElevations[country].count,
    }));
  };

  const findAirportsWithoutIATA = (airports) => {
    return airports.filter((airport) => !airport.iata || airport.iata.trim() === '');
  };

  const findMostCommonTimeZones = (airports) => {
    const timeZoneCount = {};
    airports.forEach((airport) => {
      const { tz } = airport;
      timeZoneCount[tz] = (timeZoneCount[tz] || 0) + 1;
    });
    return Object.keys(timeZoneCount)
      .map((tz) => ({ tz, count: timeZoneCount[tz] }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 10);
  };

  // Fetch airports
  useEffect(() => {
    fetchAirports();
  }, [page, sortField]);

  const fetchAirports = () => {
    setLoading(true);
    axios
      .get(`http://localhost:8080/api/airports/page?page=${page}&size=${pageSize}&sortBy=${sortField}`)
      .then((response) => {
        const fetchedAirports = response.data.content;
        setAirports(fetchedAirports);
        setTotalPages(response.data.totalPages);
        setAverageElevations(calculateAverageElevation(fetchedAirports));
        setAirportsWithoutIATA(findAirportsWithoutIATA(fetchedAirports));
        setMostCommonTimeZones(findMostCommonTimeZones(fetchedAirports));
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
      .get(`http://localhost:8080/api/airports/filter-by-name?name=${searchName}`)
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
    setSortField(field);
  };

  return (
    <div className="container">
      <h1>Airport Dashboard</h1>

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
      </div>

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
                      <th key={field} onClick={() => handleSort(field)}>
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

          {/* Analysis Sections */}
          <div className="analysis">
            <div className="analysis-card">
              <h2>
                <i className="fas fa-globe"></i> Average Elevation per Country
              </h2>
              <ul>
                {averageElevations.map((elevation) => (
                  <li key={elevation.country}>
                    {elevation.country}: {elevation.averageElevation.toFixed(2)} ft
                  </li>
                ))}
              </ul>
            </div>

            <div className="analysis-card">
              <h2>
                <i className="fas fa-plane"></i> Airports Without IATA Codes
              </h2>
              <ul>
                {airportsWithoutIATA.map((airport) => (
                  <li key={airport.icao}>
                    {airport.name} ({airport.city}, {airport.country})
                  </li>
                ))}
              </ul>
            </div>

            <div className="analysis-card">
              <h2>
                <i className="fas fa-clock"></i> Top 10 Time Zones
              </h2>
              <ul>
                {mostCommonTimeZones.map((zone) => (
                  <li key={zone.tz}>
                    {zone.tz}: {zone.count} airports
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default App;