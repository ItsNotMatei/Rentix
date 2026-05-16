import { Navigate } from 'react-router-dom';
import { getStoredUser, hasRole } from '../services/api';

export default function AdminRoute({ children, minRole = 'MODERATOR' }) {
    const user = getStoredUser();
    const token = localStorage.getItem('accessToken');
    if (!user || !token) {
        return <Navigate to="/login" replace />;
    }
    if (!hasRole(minRole)) {
        return <Navigate to="/" replace />;
    }
    return children;
}
