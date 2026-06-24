import Navbar from './components/common/Navbar';
import AppRoutes from './routes/AppRoutes';

export default function App() {
  return (
    <>
      <Navbar />
      <div className="container">
        <AppRoutes />
      </div>
    </>
  );
}
