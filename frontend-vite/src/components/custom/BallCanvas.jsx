import {React, useRef, useEffect} from 'react'
import { Ball } from './BallCanvas/Ball'


export default function BallCanvas(props) {
  
    const canvasRef = useRef(null)    

    let balls;

    
  
    const draw = (ctx, scale) => {

      ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
      ctx.fillStyle = '#01621c'

      for (const b of balls) {
        b.update(ctx.canvas.height, ctx.canvas.width, scale);
        b.draw(ctx);
      }
      
      

    }
    
    useEffect(() => {
      
      const canvas = canvasRef.current
      const context = canvas.getContext('2d')

      const scale = window.devicePixelRatio; // Adjust for device pixel ratio
      canvas.style.width = "100%";
      canvas.style.height = "100%";
      canvas.width = canvas.offsetWidth;
      canvas.height = canvas.offsetHeight;

      if (balls == undefined) {
        balls = [];
        for (let i = 0; i < 10; i++) {
          balls.push(new Ball(
            Math.random() * canvas.width,
            10 + Math.random() * canvas.height / 2,
            Math.random() * 5,
            Math.random() * 2,
            Math.random() * 3,
            5 + Math.floor(Math.random() * 5))
          );
        }
      }
      let frameCount = 0
      let animationFrameId
      
      const render = () => {
        frameCount++
        draw(context, scale)
        animationFrameId = window.requestAnimationFrame(render)
      }
      render()
      
      return () => {
        window.cancelAnimationFrame(animationFrameId)
      }
    }, [draw])
    
    return <canvas ref={canvasRef} {...props}/>
}