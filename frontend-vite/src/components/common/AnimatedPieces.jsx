export function generateAnimatedPiece(orientation, offset, color) {
    // orientation: left -> true, right -> false
    if (orientation) { // Piece Left
        return ( // ( 1 ) Offset se usa como key para evitar warnings de react
            <div className='horizontal-default' key={`left-${offset}`}>
                <div className='effect2' style={{ animationDelay: offset / 1 + 's', backgroundColor: color }} />
                <div className='effect1' style={{ animationDelay: offset / 1 + 's', backgroundColor: color }} />
            </div>
        );
    } // Piece right
    return ( // Ver ( 1 )
        <div className='horizontal-default' key={`right-${offset}`}>
            <div className='effect1' style={{ animationDelay: offset / 1 + 's', backgroundColor: color }} />
            <div className='effect2' style={{ animationDelay: offset /  + 's', backgroundColor: color }} />
        </div>
    );
}
