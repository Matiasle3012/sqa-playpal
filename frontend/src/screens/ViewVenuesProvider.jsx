import Sidebar from '../components/custom/Sidebar'; // Import the Sidebar component
import VenueCard from "../components/common/VenueCard"; // Import the VenueCard component
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome' // Import FontAwesomeIcon
import { faPlus } from '@fortawesome/free-solid-svg-icons' // Import FontAwesomeIcon
import { Routes, Route, Outlet, useLocation } from 'react-router-dom';

function ViewVenuesProvider(){
    const location = useLocation();
    const isExactParentRoute = location.pathname === '/my-venues';

    return(
        <>
        { isExactParentRoute ? (
            <>
                <Sidebar />
                <div id='contentsSectionMyReservations' className="p-4 w-[85%] ml-[15%] mt-[5%] flex flex-col justify-center -translate-y-10">
                    <h1 className="text-6xl m-2 -translate-y-5">My Venues</h1>
                    <div className="grid grid-cols-4 gap-2 align-middle">
                        <div className="col-span-3 min-h-10 "></div>
                        <div id="viewVenuesProviderCreateNewVenueButton" className="cursor-pointer min-h-10 border-4 border-[var(--color-accent)] text-[var(--color-accent)] font-extrabold text-2xl"> <FontAwesomeIcon icon={faPlus} /> CREATE</div>
                    </div>
                    <div id='viewVenuesProviderVenuesDisplayArea' className="flex flex-col w-full pr-5">
                        <VenueCard venueName='La Caimanera' venueCity='San Antonio' venueStreet='St. Simon Rodriguez' venueCourtsNumber='5' />
                    </div>
                </div>
            </>
        
    ):(
        <Outlet />
    )}
    </>);
}

export default ViewVenuesProvider;