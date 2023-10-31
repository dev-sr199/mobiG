const toggleButton = document.getElementById('toggleButton');
const myDiv = document.getElementById('hideThis');

toggleButton.addEventListener('click', ()=>{

    if (myDiv.style.display == 'none') {

        myDiv.style.display = 'block';
    } else {

        myDiv.style.display = 'none';

    }

});