import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router'
import Index from './screens/Index'

const Login = lazy(()=> import('./screens/Login'));
const Browse = lazy(()=> import('./screens/Browse'));
const ViewReservationsUser = lazy(()=> import('./screens/ViewReservationsUser'));
const ViewVenuesProvider = lazy(()=> import('./screens/ViewVenuesProvider'));
const ViewCourtsVenueProvider = lazy(()=> import('./screens/ViewCourtsVenueProvider'));

function App() {
  /*const [count, setCount] = useState(0)*/

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Index />} />
        <Route path="/browse" element={<Browse />} />
        <Route path="/my-reservations" element={<ViewReservationsUser />} />
        <Route path="/my-venues" element={<ViewVenuesProvider />} >
          <Route path='/my-venues:id' element={<ViewCourtsVenueProvider venueName="La Caimanera" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
