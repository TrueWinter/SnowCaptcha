(function() {
    function handleScreenSize() {
        if (document.querySelector('.navbar') === null) return;

        let navItems = document.getElementsByClassName('navbar-item');

        if (window.innerWidth > 600) {
            document.querySelector('.navbar').style.minHeight = null;

            for (var i = 0; i < navItems.length; i++) {
                navItems[i].classList.remove('navbar-hidden');
                navItems[i].classList.remove('navbar-shown');
            }

            return;
        }

        document.querySelector('.navbar').style.minHeight = `calc(1em + 16px + (2 * 8px))`
    }

    handleScreenSize();

    window.addEventListener('resize', () => {
        handleScreenSize();
    });

    var navIsHidden = true;
    if (document.querySelector('.navbar-collapse')) {
        document.querySelector('.navbar-collapse').addEventListener('click', (e) => {
            e.preventDefault();
            let navItems = document.getElementsByClassName('navbar-item');
            if (navIsHidden) {
                for (var i = 0; i < navItems.length; i++) {
                    navItems[i].classList.remove('navbar-hidden')
                    navItems[i].classList.add('navbar-shown');
                }
                navIsHidden = false;
            } else {
                for (var i = 0; i < navItems.length; i++) {
                    navItems[i].classList.add('navbar-hidden')
                    navItems[i].classList.remove('navbar-shown');
                }
                navIsHidden = true;
            }
        });
    }
})();