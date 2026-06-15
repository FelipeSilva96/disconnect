import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { AppLayout } from "@/layouts/AppLayout";
import { LandingPage } from "@/pages/Landing";
import { HomePage } from "@/pages/Home";
import { CreateEventPage } from "@/pages/events/CreateEvent";
import { EditEventPage } from "@/pages/events/EditEvent";
import { EventDetailsPage } from "@/pages/events/EventDetails";
import { MyEventsPage } from "@/pages/events/MyEvents";
import { ProfilePage } from "@/pages/Profile";
import { EditProfilePage } from "@/pages/Profile/EditProfile";
import { DeleteProfilePage } from "@/pages/Profile/DeleteProfile";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Rotas Públicas */}
        <Route path="/landing" element={<LandingPage />} />

        {/* Se alguém tentar ir direto pela URL, mandamos para a Landing para abrir a tela de login */}
        <Route path="/login" element={<Navigate to="/landing" replace />} />
        <Route path="/register" element={<Navigate to="/landing" replace />} />

        {/* Rotas Protegidas (Só entra se logar) */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<HomePage />} />
            {/* A busca agora vive no header e filtra a Home */}
            <Route path="/search" element={<Navigate to="/" replace />} />
            <Route path="/events/create" element={<CreateEventPage />} />
            <Route path="/events/:id/edit" element={<EditEventPage />} />
            <Route path="/events/:id" element={<EventDetailsPage />} />
            <Route path="/events" element={<MyEventsPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/profile/:id" element={<ProfilePage />} />
            <Route path="/profile/edit" element={<EditProfilePage />} />
            <Route path="/profile/delete" element={<DeleteProfilePage />} />
          </Route>
        </Route>

        {/* Fallback (Rota não encontrada vai para a Landing) */}
        <Route path="*" element={<Navigate to="/landing" replace />} />
      </Routes>
    </AuthProvider>
  );
}
