import Sidebar from '../components/custom/Sidebar'; // Import the Sidebar component
import ReserveCard from '../components/common/ReserveCard'; // Import the ReserveCard component

function ViewReservationsUser() {
    return (
        <>
            <Sidebar />
            <div id='contentsSectionMyReservations' className="p-4 w-[85%] ml-[15%] mt-[5%] flex flex-col justify-center -translate-y-10">
                <h1 className="text-6xl m-2 -translate-y-5">My Reservations</h1>
                <div className="grid grid-cols-4 gap-2 align-middle">
                    <div id='viewActiveReservationsMyReservations' className="cursor-pointer min-h-10 border-4 border-[var(--color-main)] bg-[var(--color-main)] text-[var(--color-highlight)] font-extrabold">ACTIVAS</div>
                    <div id='viewPastReservationsMyReservations' className="cursor-pointer min-h-10 border-4 font-extrabold">PASADAS</div>
                </div>
                <div id='viewReservationsUserReservationsDisplayArea' className="flex flex-col w-full pr-5">
                    <ReserveCard name='Cancha1' location='Los Altos' venue='La Caimanera' sport='Soccer' date='26/05/2025' time='08:00' price="2$" />
                    <ReserveCard name='Cancha1' location='Los Altos' venue='La Caimanera' sport='Soccer' date='26/05/2025' time='08:00' price="2$" />
                    <ReserveCard name='Cancha1' location='Los Altos' venue='La Caimanera' sport='Soccer' date='26/05/2025' time='08:00' price="2$" />
                </div>
            </div>
        </>
    );
}

export default ViewReservationsUser;