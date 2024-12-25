# Medical Assistant AI

A Spring Boot application that provides an AI-powered medical consultation interface using the Mixtral-8x7B language model.

## Features

- 🏥 Interactive Medical Chat
- 🔍 AI-Powered Symptom Analysis
- 💉 Preliminary Diagnosis Assistance
- 📱 Responsive Design
- 🔒 Secure API Integration

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- HuggingFace API key

## Quick Start

1. Clone the repository:
```bash
git clone https://github.com/yourusername/medical-assistant.git
cd medical-assistant
```

2. Configure your HuggingFace API key:
   - Open `src/main/resources/application.properties`
   - Replace `huggingface.api.key=your_api_key_here` with your actual API key

3. Build and run:
```bash
mvn spring-boot:run
```

4. Access the application:
   - Open http://localhost:8080 in your browser

## Technical Stack

- **Backend**: Spring Boot 3.1.0
- **Frontend**: HTML5, CSS3, jQuery
- **API Integration**: OkHttp3
- **Model**: Mixtral-8x7B-Instruct-v0.1
- **Template Engine**: Thymeleaf

## Important Notice

This application is for educational and demonstration purposes only. It should not be used as a substitute for professional medical advice, diagnosis, or treatment. Always seek the advice of qualified healthcare providers.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── medical/
│   │           ├── config/
│   │           ├── controller/
│   │           └── service/
│   └── resources/
│       ├── templates/
│       └── application.properties
```

## API Endpoints

- `POST /api/chat`: Medical chatbot interaction
- `POST /api/diagnose`: Symptom analysis and preliminary diagnosis

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Acknowledgments

- [Mixtral-8x7B-Instruct-v0.1](https://huggingface.co/mistralai/Mixtral-8x7B-Instruct-v0.1)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [HuggingFace](https://huggingface.co/)
