import { Navigate } from 'react-router-dom';
import { getStoredUser } from '../services/api';

export default function ProtectedRoute({ children }) {
    const user = getStoredUser();
    const token = localStorage.getItem('accessToken');
    if (!user || !token) {
        return <Navigate to="/login" replace />;
    }
    return children;
}
