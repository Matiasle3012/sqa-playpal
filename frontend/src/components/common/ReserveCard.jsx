import { FontAwesomeIcon } from '@fortawesome/react-fontawesome' // Import FontAwesomeIcon
import { faImage } from '@fortawesome/free-solid-svg-icons' // Import FontAwesomeIcon

function ReserveCard({ name, location, venue, sport, date, time, price}){
    return (
        <div className="flex w-full h-auto min-h-30 m-3 p-5 justify-center border-2 border-[var(--color-main)] rounded-sm border-0 text-2xl text-[var(--color-main)] p-2 shadow-xl/20">
            <div className="flex-1 text-9xl"> <FontAwesomeIcon icon={faImage} /> </div>
            <div className="flex-1 grid grid-cols-2 gap-2 ">
                <h1 className="col-span-2 text-3xl">{name}</h1>
                <div className="text-xl">{location}</div>
                <div className="text-xl">{venue}</div>
                <div className="text-xl">{sport}</div>
                <div className="text-xl">{date}</div>
                <div className="text-xl">{time}</div>
                <div className="text-xl">{price}</div>
            </div>
        </div>
    );
}

export default ReserveCard;