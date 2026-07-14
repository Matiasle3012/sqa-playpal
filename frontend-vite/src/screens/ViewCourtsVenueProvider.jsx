import Sidebar from '../components/custom/Sidebar'; // Import the Sidebar component
import CourtCard from "../components/common/CourtCard"; // Import the VenueCard component
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome' // Import FontAwesomeIcon
import { faPlus } from '@fortawesome/free-solid-svg-icons' // Import FontAwesomeIcon

function ViewCourtsVenueProvider({ venueName }) {
  return (
    <>
      <Sidebar />
    <div id='contentsSectionBrowse' className="p-4 w-[85%] ml-[15%] mt-[5%]">
        <h1 className="text-6xl m-2 -translate-y-5"> {venueName} </h1>
        <div className="grid grid-cols-4 gap-2 align-middle">
            <div className="col-span-3 min-h-10 "></div>
            <div id="viewCourtsVenueProviderCreateNewCourtButton" className="cursor-pointer min-h-10 border-4 border-[var(--color-accent)] text-[var(--color-accent)] font-extrabold text-2xl"> <FontAwesomeIcon icon={faPlus} /> CREATE</div>
        </div>
        <div id='browseCourtsVenueProviderDisplayArea' className="p-4 border-none grid grid-cols-4 gap-4 mb-4">
            <CourtCard courtName='Los Castores' courtLocation='Los Altos' courtType='Grass' courtPrice='5$'/>
            <CourtCard courtName='Los Alpes' courtLocation='Los Altos' courtType='Concrete' courtPrice='2$'/>
            <CourtCard courtName='La Cueva' courtLocation='Caracas' courtType='Grass' courtPrice='10$'/>
            <CourtCard courtName='Los Castores' courtLocation='Los Altos' courtType='Grass' courtPrice='5$'/>
            <CourtCard courtName='Los Alpes' courtLocation='Los Altos' courtType='Concrete' courtPrice='2$'/>
            <CourtCard courtName='La Cueva' courtLocation='Caracas' courtType='Grass' courtPrice='10$'/>
            <CourtCard courtName='Los Castores' courtLocation='Los Altos' courtType='Grass' courtPrice='5$'/>
            <CourtCard courtName='Los Alpes' courtLocation='Los Altos' courtType='Concrete' courtPrice='2$'/>
            <CourtCard courtName='La Cueva' courtLocation='Caracas' courtType='Grass' courtPrice='10$'/>
            <CourtCard courtName='Los Castores' courtLocation='Los Altos' courtType='Grass' courtPrice='5$'/>
            <CourtCard courtName='Los Alpes' courtLocation='Los Altos' courtType='Concrete' courtPrice='2$'/>
            <CourtCard courtName='La Cueva' courtLocation='Caracas' courtType='Grass' courtPrice='10$'/>
        </div>
    </div>
    </>
  );
}

export default ViewCourtsVenueProvider;