import CourtCard from '../components/common/CourtCard'; // Import the Card component
import SearchFilter from '../components/custom/SearchFilter'; // Import the SearchFilter component
import Sidebar from '../components/custom/Sidebar'; // Import the Sidebar component
import ReserveCard from '../components/common/ReserveCard';

function Browse() { 
    return(
    <>
    <Sidebar />
    <div id='contentsSectionBrowse' className="p-4 w-[85%] ml-[15%] mt-[5%]">
        <SearchFilter />
        <div id='browseCourtsDisplayArea' className="p-4 border-none grid grid-cols-4 gap-4 mb-4">
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
</>)}

export default Browse;