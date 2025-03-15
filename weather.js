function formatDate(date) {
  const d = new Date(date);
  const day = String(d.getDate()).padStart(2, "0");
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const year = d.getFullYear();
  return `${year}-${month}-${day}`;
}

function getWeatherData() {
  const pincode = document.getElementById("pincode").value;
  const dateInput = document.getElementById("date").value;

  if (!pincode || !dateInput) {
    showError("Please enter both pincode and date");
    return;
  }

  if (!/^\d{6}$/.test(pincode)) {
    showError("Please enter a valid 6-digit pincode");
    return;
  }

  const formattedDate = formatDate(dateInput);
  const resultDiv = document.getElementById("result");
  resultDiv.innerHTML = "<p>Loading weather information...</p>";

  fetch(`/api/weather/${pincode}/${formattedDate}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((apiResponse) => {
      if (apiResponse.errors) {
        const errorMessage = Object.values(apiResponse.errors).join(", ");
        showError(errorMessage);
        return;
      }

      const weatherData = apiResponse.data;
      resultDiv.innerHTML = `
        <div class="weather-info">
          <h3>Weather Information for ${pincode}</h3>
          <p><strong>Temperature:</strong> ${weatherData.temperature}°C</p>
          <p><strong>Description:</strong> ${weatherData.description}</p>
          <p><strong>Date:</strong> ${formattedDate}</p>
          <p><small>Source: ${weatherData.source}</small></p>
        </div>
      `;
    })
    .catch((error) => {
      showError(error.message);
    });
}

function showError(message) {
  document.getElementById("result").innerHTML = `
    <div class="error">
      <p>Error: ${message}</p>
    </div>
  `;
}

// Set max date to today
document.addEventListener("DOMContentLoaded", function () {
  const today = new Date().toISOString().split("T")[0];
  document.getElementById("date").max = today;
});
