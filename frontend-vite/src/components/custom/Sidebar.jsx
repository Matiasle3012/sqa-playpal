import "../../styles/index.css"
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faHouse, faUser, faGear } from '@fortawesome/free-solid-svg-icons'
// import { faCircleUser } from '@fortawesome/free-regular-svg-icons'
// Dejar esto aqui para que tengamos la otra libreria de iconos

function sideLabel( img, label ) {
    return (
        <li className="flex items-center p-2 text-[var(--color-bg)] rounded-lg hover:bg-[#5A6A62] hover:text-white dark:hover:bg-gray-700 group">
            {img}
            <span class="ms-3">{label}</span>
        </li>
    )
}

function Sidebar() {
    return (
        <div className="h-screen w-1/7 fixed left-0 top-0 bg-[var(--color-main)] text-[var(--color-bg)] flex flex-col justify-between">
            <div>
                <div className="h-1/6 w-full flex items-center justify-center gap-2 pt-4">
                    {/* <img src="null" className="h-3/4 w-auto" alt="Logo" /> */}
                    <p className="text-2xl font-bold">PlayPal</p>
                </div>
                <div className="h-5/6 w-full flex flex-col items-center justify-start gap-4 pt-4">
                    <span className="w-4/5 h-12 flex items-center justify-center rounded-sm bg-transparent">
                        <a href="/home" className="transition-all duration-200 hover:text-[var(--color-highlight)] hover:scale-110 hover:brightness-125 flex items-center gap-[10%]">
                            <FontAwesomeIcon icon={faHouse} />
                            Home
                        </a>
                    </span>
                    <span className="w-4/5 h-12 flex items-center justify-center rounded-sm bg-transparent">
                        <a href="/profile" className="transition-all duration-200 hover:text-[var(--color-highlight)] hover:scale-110 hover:brightness-125 flex items-center gap-[10%]">
                            <FontAwesomeIcon icon={faUser} />
                            Profile
                        </a>
                    </span>
                    <span className="w-4/5 h-12 flex items-center justify-center rounded-sm bg-transparent">
                        <a href="/settings" className="transition-all duration-200 hover:text-[var(--color-highlight)] hover:scale-110 hover:brightness-125 flex items-center gap-[10%]">
                            <FontAwesomeIcon icon={faGear} />
                            Settings
                        </a>
                    </span>
                </div>
            </div>
            <div className="w-full flex items-center justify-center pb-4">
                <button 
                    onClick={() => window.location.href = '/login'} 
                    className="cursor-pointer w-auto px-4 py-2 text-center rounded-2xl border-2 border-[var(--color-highlight)] text-[var(--color-main)] bg-[var(--color-bg)] hover:bg-[var(--color-main)] hover:text-[var(--color-bg)] hover:border-[var(--color-bg)] transition-colors duration-200"
                >
                    Log Out
                </button>
            </div>
        </div>
    );
}

export default Sidebar;