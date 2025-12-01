import React from "react";
import { Link } from "react-router-dom";

const SecondaryNavbar = () => {
    return (
        <>
            <nav className="secondary-navbar">
                <ul className="nav-links">
                    <li>
                        <Link to="/portfolio">Portfolio</Link>
                    </li>
                    <li>
                        <Link to="/reviews">Reviews</Link>
                    </li>
                </ul>
            </nav>

            <style>
                {`
                .secondary-navbar {
                    background-color: #000;
                    color: #fff;
                    padding: 0.5rem 0;
                    text-align: center;
                }

                .secondary-navbar .nav-links {
                    display: flex;
                    justify-content: center;
                    gap: 2rem;
                    list-style: none;
                    margin: 0;
                    padding: 0;
                }

                .secondary-navbar .nav-links li a {
                    color: #fff;
                    text-decoration: none;
                    font-weight: bold;
                    font-size: 1rem;
                    transition: color 0.3s ease;
                }

                .secondary-navbar .nav-links li a:hover {
                    color: #fbbf24; /* yellow */
                }

                @media (max-width: 640px) {
                    .secondary-navbar .nav-links {
                        flex-direction: column;
                        gap: 1rem;
                    }
                }
                `}
            </style>
        </>
    );
};

export default SecondaryNavbar;
