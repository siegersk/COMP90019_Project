using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;



namespace DistComputingProject.Controllers
{
    public class Example : Controller
    {
        
        public IActionResult Index()
        {
            return View();
        }

        public IActionResult Example2()
        {
            return View();
        }

        public IActionResult Example3()
        {
            return View();
        }
    }
}
