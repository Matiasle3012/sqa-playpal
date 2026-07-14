import { FontAwesomeIcon } from '@fortawesome/react-fontawesome' // Import FontAwesomeIcon
import { faImage } from '@fortawesome/free-solid-svg-icons' // Import FontAwesomeIcon

function VenueCard({ venueName, venueCity, venueStreet, venueCourtsNumber}){
    return (
        <div className="flex w-full h-auto min-h-30 m-3 p-5 justify-center border-2 border-[var(--color-main)] rounded-sm border-0 text-2xl text-[var(--color-main)] p-2 shadow-xl/20">
            <h1 className="text-center basis-1/3 text-3xl"> <FontAwesomeIcon icon={faImage} /> </h1>
            <div className="pt-2 items-start flex flex-col gap-2 basis-2/3">
                <p className="self-center text-5xl font-bold">{venueName}</p>
                <p className="self-center text-sm">{venueCity} , {venueStreet}</p>
                <p className="self-center text-sm">"Numero de Canchas: {venueCourtsNumber}</p>
                <div className="cursor-pointer self-center min-h-10 border-4 pt-2 pb-2 pl-4 pr-4 border-[var(--color-main)] bg-[var(--color-main)] text-[var(--color-highlight)]">Ver Detalles</div>
            </div>
        </div>
    );
}

export default VenueCard;