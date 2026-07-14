import SpinningCube from '../components/custom/SpinningCube';
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

export default function Login() {

    let animatedPiecesL = [];
    let animatedPiecesR = [];
    let flip = false;
    for (let i = 0; i < 5; i++) {
        if (!flip) {
            animatedPiecesL.push(generateAnimatedPiece(true, i));
            animatedPiecesR.push(generateAnimatedPiece(false, i));
        } else {
            animatedPiecesR.push(generateAnimatedPiece(true, i));
            animatedPiecesL.push(generateAnimatedPiece(false, i));
        }
        flip = !flip;
    }
    return (
        <>
            {/*
             ( 2 ) login-container agrupa el fondo con cualquier otro elemento en paralelo
             para que se muestre uno sobre el otro, como algun Canvas o un video
             */}
            <div className='login-container'>
                <SpinningCube />
                <div className='background-l'>
                    {animatedPiecesL}
                </div>
                <div className='background-r'>
                    {animatedPiecesR}
                </div>

                <div className='margin'>
                    <h1>LOGIN</h1>
                    <div className='fade-in' id='loginContainer'>

                        {/* se usa el mismo className de un componente grafico que un elemento interactivo */}
                        <div className='horizontal-default shake animated-text-float login-input-field'>
                            <label>Username</label>
                            <input className='highlight' type='text' onChange={onInputTextChanged} id='username-input'/>
                        </div>
                        {/* se usa el mismo className de un componente grafico que un elemento interactivo */}
                        <div className='horizontal-default shake animated-text-float login-input-field'>
                            <label>Password</label>
                            <input className='highlight' onChange={onInputTextChanged} type='password' id='password-input'/>
                        </div>
                        <button className='highlight' onClick={onLoginPress}>LOGIN</button>
                    </div>
                </div>
            </div>
        </>
    );
}