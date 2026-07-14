import { FontAwesomeIcon } from '@fortawesome/react-fontawesome' // Import FontAwesomeIcon
import { faImage } from '@fortawesome/free-solid-svg-icons' // Import FontAwesomeIcon

function CourtCard({courtName, courtType, courtLocation, courtPrice}) {
    return (
        <div className=" flex flex-col w-full h-auto min-h-20 items-start text-start border-2 border-[var(--color-main)] rounded-sm border-0 text-2xl text-[var(--color-main)] p-2 shadow-xl/20">
            <h1 className="text-start w-full"> <FontAwesomeIcon icon={faImage} /> </h1>
            <div className="pt-2 items-start text-left">
                <p className="text-3xl font-bold">{courtName}</p>
                <p className="text-sm">{courtLocation}</p>
                <p className="text-sm">{courtType}</p>
                <p className="text-xs">{courtPrice}/h</p>
            </div>
        </div>
    );
}

export default CourtCard;