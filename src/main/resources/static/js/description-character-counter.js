document.addEventListener('DOMContentLoaded', function() {
	const textarea = document.getElementById('descriptionTextarea');
	const charCount = document.getElementById('charCount');
	
	if (textarea && charCount) {
		charCount.textContent = textarea.value.length;
		
		textarea.addEventListener('input', function() {
			const currentLength = this.value.length;
			charCount.textContent = currentLength;
			
			if (currentLength > 450) {
				charCount.style.color = '#dc3545';
			} else if (currentLength > 400) {
				charCount.style.color = '#fd7e14';
			} else {
				charCount.style.color = '#6c757d';
			}
			
		});
	}
});