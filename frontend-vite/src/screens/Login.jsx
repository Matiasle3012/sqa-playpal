import BallCanvas from '../components/custom/3dCanvasBalls';
import { generateAnimatedPiece } from "../components/common/AnimatedPieces";


function onLoginPress(event) {
    let elements = event.target.parentNode.children;
    for (let match of elements) {
        if (match.classList.contains("shake")) {
            match.classList.remove("error-password-animation");
            void match.offsetHeight;
            match.classList.add("error-password-animation");
        }
    }
    let input_fields = document.getElementsByClassName('login-input-field');
    for (let item of input_fields) {
        let change_pending = [];
        for (let element of item.children) {
            if (element.tagName.toLowerCase() === 'input') {
                element.value = '';              
            } else if (element.tagName.toLowerCase() === 'label') {
                change_pending.push(element); 
                /* 
                ( 3 ) No se puede cambiar AQUI porque el for de arriba no se ha completado
                y al agregar un elemento Child a Item, se lee en una proxima iteracion y se duplica.

                Se debe agregar a la lista de cambios pendientes y ejecutarse al final de haber recogido
                todos los elementos.
                */
            }
        }
        change_pending.forEach((element) => { // Ver ( 3 )
            let prev_content = element.textContent;
            item.removeChild(element);
            let newLabel = document.createElement('label');
            newLabel.textContent = `${prev_content}`;
            item.appendChild(newLabel);
        });
    }

}

function onInputTextChanged(event) {
    const labels = Array.from(event.target.parentNode.children)
        .filter(child => child.tagName.toLowerCase() === 'label');
    
        labels.forEach(element => {
            if (event.target.value) {
                element.style.transform = 'translateX(-150%)';
                element.style.transition = 'transform 0s ease 0s 1 forwards';
                event.target.style.contentVisibility = 'visible';
            }
    });
    
}

function registerTransition(event) {
    let sidebar = document.getElementById("sidebar");
    let switchMessage = document.getElementById("loginRegisterChange")
    let mainpanel = document.getElementById("mainPanelLogin");
    let bgL = document.getElementById("background-l");
    let bgR = document.getElementById("background-r");
    let ma = document.getElementById("loginActionButton");
    let title = document.getElementById("title");
    let playpalT = document.getElementById('playpalLoginText')
    if (sidebar) {
        sidebar.classList.toggle('fly-by-left');
    }
    if (mainpanel) {
        mainpanel.classList.toggle("fly-by-right");
    }

    for (let element1 of bgL.children) {
        for (let child of element1.children) {
            let bgcol = child.style.backgroundColor;
            if (bgcol == "rgb(253, 249, 241)") {
                child.style.backgroundColor = "#016b1c";
            } else {
                child.style.backgroundColor = "#fdf9f1";
            } 
        }
    }
    for (let element2 of bgR.children) {
        for (let child of element2.children) {
            let bgcol = child.style.backgroundColor;
            if (bgcol == "rgb(253, 249, 241)") {
                child.style.backgroundColor = "#016b1c";
            } else {
                child.style.backgroundColor = "#fdf9f1";
            }        
        }
    }
    if (ma.textContent == "LOGIN") {
        ma.textContent = "REGISTER";
        title.textContent = "Sign up";
        switchMessage.textContent = "Have an account already? Login";
        playpalT.classList.add('right-[20%]');
        playpalT.classList.remove('left-[10%]');
    } else {
        ma.textContent = "LOGIN";
        title.textContent = "Sign in";
        switchMessage.textContent = "Don't have an account? Register";
        playpalT.classList.add('left-[10%]');
        playpalT.classList.remove('right-[20%]');
    }
}

export default function Login() {

    let animatedPiecesL = [];
    let animatedPiecesR = [];
    let flip = false;
    for (let i = 0; i < 4; i++) {
        if (!flip) {
            animatedPiecesR.push(generateAnimatedPiece(false, i));
            animatedPiecesL.push(generateAnimatedPiece(true, i, "#fdf9f1"));
        } else {
            animatedPiecesR.push(generateAnimatedPiece(true, i));
            animatedPiecesL.push(generateAnimatedPiece(false, i, "#fdf9f1"));
        }
        flip = !flip;
    }
    return (
        <>
            {/*
                ( 2 ) overlay-container agrupa el fondo con cualquier otro elemento en paralelo
                para que se muestre uno sobre el otro, como algun Canvas o un video
             */}
            <div className='overlay-container'>
                <div className='background-l z-2 justify-center' id='background-l'>
                    {animatedPiecesL}
                </div>
                <div className='background-r z-2 justify-center' id='background-r'>
                    {animatedPiecesR}
                </div>

                <div className='margin w-full h-full'>
                    <div className="flex flex-row w-[100%] h-full w-full items-center justify-start">
                        <div id="sidebar" className="bg-[var(--color-main)] w-[40%] h-full flex items-center justify-center text-[var(--color-bg)] pt-[2.5%] movable z-1">
                            <h1 id='playpalLoginText' className='relative left-[10%]'>PlayPal</h1>
                        </div>
                        <div id='mainPanelLogin' className="bg-transparent h-full w-[60%] items-center justify-center flex flex-col movable">
                            <h1 id='title'>Sign in</h1>
                            <div id='loginContainer' className='fade-in mt-5'>
                                {/* se usa el mismo className de un componente grafico que un elemento interactivo */}
                                <div className='login-div shake animated-text-float login-input-field'>
                                    <label>Username</label>
                                    <input id='usernameInputLogin' className='highlight' type='text' onChange={onInputTextChanged} />
                                </div>
                                {/* se usa el mismo className de un componente grafico que un elemento interactivo */}
                                <div className='login-div shake animated-text-float login-input-field'>
                                    <label>Password</label>
                                    <input id='passwordInputLogin' className='highlight' onChange={onInputTextChanged} type='password' />
                                </div>
                            </div>
                            <div className="w-[max-content] flex flex-col items-center justify-center gap-2">
                            <button id='loginActionButton' className='highlight w-[min-content]' onClick={onLoginPress} >LOGIN</button>
                            <a id='loginRegisterChange' onClick={registerTransition}>Don't have an account? Register</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}