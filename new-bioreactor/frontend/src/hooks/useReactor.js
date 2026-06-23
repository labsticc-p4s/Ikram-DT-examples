import { useState, useEffect } from 'react';
import axios from 'axios';
import { PH } from '../helpers/constants';

export function useReactor() {
    const [phys,        setPhys]        = useState({ reactorId: '', sensorReadings: {} });
    const [registered,  setRegistered]  = useState(false);
    const [registering, setRegistering] = useState(false);
    const [flash,       setFlash]       = useState(null);

    const showFlash = (msg, ok = true) => {
        setFlash({ msg, ok });
        setTimeout(() => setFlash(null), 2500);
    };

    const fetchPhys = async () => {
        try { const r = await axios.get(`${PH}/state`); setPhys(r.data); } catch (_) {}
    };

    const register = async () => {
        setRegistering(true);
        try {
            await axios.post(`${PH}/register`);
            setRegistered(true);
            showFlash('Bioreactor registered!');
            await fetchPhys();
        } catch (_) {
            showFlash('Registration failed', false);
        } finally {
            setRegistering(false);
        }
    };

    useEffect(() => {
        fetchPhys();
        const id = setInterval(fetchPhys, 1000);
        return () => clearInterval(id);
    }, []);

    return { phys, registered, registering, register, flash };
}
