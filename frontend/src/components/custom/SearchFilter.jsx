import React from "react";
import { useState, useEffect, useRef } from 'react';
import "../../styles/index.css";

/*
function subtitle(innerText) {
    return (
        <div className="flex items-center justify-center w-9/10 h-full text-[var(--color-main)] rounded-sm font-medium transition-all duration-200 hover:text-[var(--color-main)] hover:scale-110 hover:brightness-125">
            {innerText}
        </div>
    );
}


function initialSearch() {
    return (
        <div className="h-48 w-auto mb-5 pt-4 pb-4 justify-items-center rounded-sm bg-transparent">
            <div className="grid grid-cols-2 gap-2 w-full content-center justify-items-center">
                <input 
                    type="date" 
                    id="dateSelectionSearchFilter" 
                    className="bg-[var(--color-highlight)] w-9/10 h-full rounded-sm text-center text-[var(--color-main)] border-2 border-[var(--color-main)] focus:border-[var(--color-main)] focus:outline-none p-2"
                />
                <select 
                    name="sports" 
                    id="sportSelectionSearchFilter" 
                    className="bg-[var(--color-highlight)] w-9/10 h-full rounded-sm text-center text-[var(--color-main)] border-2 border-[var(--color-main)] focus:border-[var(--color-main)] focus:outline-none p-2"
                >
                    <option value="default" disabled selected hidden>-</option>
                    <option value="soccer">Soccer</option>
                    <option value="basketball">Basketball</option>
                    <option value="tennis">Tennis</option>
                </select>
                {subtitle('Date')}
                {subtitle('Sport')}
                <select 
                    name="branch" 
                    id="branchSelectionSearchFilter" 
                    className="bg-[var(--color-highlight)] w-9/10 h-full rounded-sm text-center text-[var(--color-main)] border-2 border-[var(--color-main)] focus:border-[var(--color-main)] focus:outline-none p-2"
                >
                    <option value="default" disabled selected hidden>-</option>
                    <option value="branch1">Branch 1</option>
                    <option value="branch2">Branch 2</option>
                    <option value="branch3">Branch 3</option>
                </select>
                <input 
                    type="text" 
                    className="bg-[var(--color-highlight)] w-9/10 h-full rounded-sm text-[var(--color-main)] border-2 border-[var(--color-main)] focus:border-[var(--color-main)] focus:outline-none p-2" 
                    placeholder="Location" 
                />
                {subtitle('Branch')}
                {subtitle('Location')}    
            </div>
            <div className="inline-block align-left pt-0 text-center h-auto rounded-xl text-[var(--color-main)] bg-[var(--color-highlight)] border-2 border-[var(--color-main)] hover:bg-[var(--color-main)] hover:text-[var(--color-highlight)] hover:border-[var(--color-highlight)] transition-colors duration-200 px-8">
                Search
            </div>
        </div>
    );
}
*/

function fixedSearch() {
    return (
        <div className="bg-[var(--color-bg)]/[0] w-[80%] ml-[0%] h-20 fixed grid grid-cols-4 pt-5 pb-5 top-0">
            <input 
                type="date" 
                id="dateSelectionSearchFilter" 
                className="bg-[var(--color-bg)] w-9/10 h-full rounded-2xl text-center text-[var(--color-main)] border-2 border-[var(--color-main)] focus:border-[var(--color-main)] focus:outline-none p-2"
            />
            <select 
                name="sports" 
                id="sportSelectionSearchFilter" 
                className="bg-[var(--color-bg)] w-9/10 h-full rounded-2xl text-center text-[var(--color-main)] border-2 border-[var(--color-main)] focus:border-[var(--color-main)] focus:outline-none p-2"
            >
                <option value="default" disabled selected hidden>Sport</option>
                <option value="soccer">Soccer</option>
                <option value="basketball">Basketball</option>
                <option value="tennis">Tennis</option>
            </select>
            <input 
                type="text" 
                className="bg-[var(--color-bg)] w-9/10 h-full placeholder-[var(--color-main)] rounded-2xl text-center text-[var(--color-main)] border-2 border-[var(--color-main)] focus:border-[var(--color-main)] focus:outline-none p-2" 
                placeholder="Location" 
            />
            <div className="inline-block align-left text-center h-auto rounded-2xl text-[var(--color-bg)] bg-[var(--color-main)] border-2 border-[var(--color-main)] transition-all duration-200 hover:text-[var(--color-highlight)] hover:scale-105 p-2">
                Search
            </div>
        </div>
    );
}

function SearchFilter() {
    const [isElementVisible, setIsElementVisible] = useState(true);
    const [shouldShowSecondElement, setShouldShowSecondElement] = useState(false);
    const observedElement = useRef(null);
    return fixedSearch();

    /*
    useEffect(() => {
        const observer = new IntersectionObserver(
            ([entry]) => {
                if (!entry.isIntersecting) {
                    setShouldShowSecondElement(true);
                } else {
                    setShouldShowSecondElement(false);
                }
                setIsElementVisible(entry.isIntersecting);
            },
            { threshold: 0.1 }
        );

        if (observedElement.current) {
            observer.observe(observedElement.current);
        }

        return () => {
            if (observedElement.current) {
                observer.unobserve(observedElement.current);
            }
        };
    }, []);

    return (
        <div>
            <div ref={observedElement}>
                {initialSearch()}
            </div>
            
            {shouldShowSecondElement && (
                <div>
                    {fixedSearch()}
                </div>
            )}
        </div>
    );
    */
}

export default SearchFilter;