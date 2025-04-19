// src/Analytics.jsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import { BASE_URL } from './config';

//const BASE_URL = 'https://airport-service.cfapps.us10-001.hana.ondemand.com/api/airports';

const COLORS = ['#8884d8', '#82ca9d', '#ffc658', '#ff8042', '#8dd1e1', '#a4de6c', '#d0ed57', '#d62728'];

const Analytics = () => {
  const [averageElevation, setAverageElevation] = useState([]);
  const [airportsWithoutIATA, setAirportsWithoutIATA] = useState([]);
  const [topTimezones, setTopTimezones] = useState([]);
  const [showTable, setShowTable] = useState(false);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    Promise.all([
      axios.get(`${BASE_URL}/average-elevation`),
      axios.get(`${BASE_URL}/without-iata`),
      axios.get(`${BASE_URL}/top-timezones`)
    ])
      .then(([avgRes, noIataRes, tzRes]) => {
        const elevationData = Object.entries(avgRes.data).map(([country, value]) => ({
          country,
          elevation: parseFloat(value.toFixed(2)),
        }));

        const timezoneData = tzRes.data.map(obj => {
          const [tz, count] = Object.entries(obj)[0];
          return { timezone: tz, count };
        });

        setAverageElevation(elevationData);
        setTopTimezones(timezoneData);
        setAirportsWithoutIATA(noIataRes.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Error fetching analytics data:', err);
        setLoading(false);
      });
  }, []);

  return (
    <div className="container">

      <h1>📊 Airport Analytics Dashboard</h1>
      <div>
       <button onClick={() => navigate('/')}>
           ← Back to Home
       </button>
      </div>

      {loading ? (
        <p>Loading data...</p>
      ) : (
        <>
          {/* Elevation Chart */}
          <section style={{ marginBottom: '2rem' }}>
            <h2>Average Elevation by Country</h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={averageElevation}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="country" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="elevation" fill="#8884d8" />
              </BarChart>
            </ResponsiveContainer>
          </section>

          {/* Timezones Chart */}
          <section style={{ marginBottom: '2rem' }}>
            <h2>Top Timezones</h2>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={topTimezones}
                  dataKey="count"
                  nameKey="timezone"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  label
                >
                  {topTimezones.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </section>

          {/* Toggle for Table */}
          <section>
            <h2>
              Airports Without IATA Code
              <button
                onClick={() => setShowTable(!showTable)}
                style={{
                  marginLeft: '1rem',
                  padding: '0.4rem 1rem',
                  background: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px',
                  cursor: 'pointer',
                }}
              >
                {showTable ? 'Hide' : 'Show'} Table
              </button>
            </h2>

            {showTable && (
              <div style={{ overflowX: 'auto', maxHeight: '400px', overflowY: 'scroll' }}>
                <table>
                  <thead>
                    <tr>
                      <th>ICAO</th>
                      <th>Name</th>
                      <th>City</th>
                      <th>State</th>
                      <th>Country</th>
                      <th>Elevation</th>
                    </tr>
                  </thead>
                  <tbody>
                    {airportsWithoutIATA.map((airport) => (
                      <tr key={airport.icao}>
                        <td>{airport.icao}</td>
                        <td>{airport.name}</td>
                        <td>{airport.city}</td>
                        <td>{airport.state}</td>
                        <td>{airport.country}</td>
                        <td>{airport.elevation}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
};

export default Analytics;
