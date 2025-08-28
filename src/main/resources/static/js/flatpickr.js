let maxDate = new Date();
maxDate.setMonth(maxDate.getMonth() + 3);

let tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);

flatpickr('#reservationDate', {
 mode: "single",
 locale: 'ja',
 dateFormat: 'Y-m-d',
 minDate: tomorrow,
 maxDate: maxDate,
 
 disable: [
	 function(date) {
		 if (typeof holidays !== 'undefined') {
			 const dayOfWeek = date.getDay();
			 return holidays.includes(dayOfWeek);
		 }
		 return false;
	 }
 ]
});