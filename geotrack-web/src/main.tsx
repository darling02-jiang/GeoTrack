import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { GeoTrackProvider } from './store/GeoTrackContext';
import './styles/global.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <GeoTrackProvider>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </GeoTrackProvider>
  </StrictMode>
);
